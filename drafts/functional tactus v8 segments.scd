(
//Server.default.options.device = "headphonesSc";
s.boot;
)
s.quit;

FluidWaveform(~src, ~indices);

(
~src = Buffer.read(s,"/Users/asherelazary/Desktop/Samples etc./Film Sounds/102060__juskiddink__finnish-sauna.wav");
//make indices buffer
~indices = Buffer(s);
//nrt onset slice of source
FluidBufOnsetSlice.processBlocking(s, ~src, metric: 9, threshold: 0.4, indices: ~indices, minSliceLength:1, action: {"found slices".postln}, );
//FluidBufNoveltySlice.processBlocking(s, ~src, indices: ~indices);
FluidWaveform(~src, ~indices);
)

(
~indices.loadToFloatArray(action: {
	arg fa;
	var spec = Buffer(s);
	var stats = Buffer(s);
	var meanfeatures = Buffer(s);
	//iterate through adjacent pairs of indices (tuple like)
	~onsetArr = fa;
	//
	fa.doAdjacentPairs{
		arg start, end, i;
		var numSamps = end - start;
		FluidBufSpectralShape.processBlocking(s, ~src, start, numSamps, features: spec, select:[\centroid]);
		FluidBufStats.processBlocking(s, spec, stats: stats, select:[\mean]);
		FluidBufCompose.processBlocking(s, stats, destination: meanfeatures, destStartFrame: i);
	};

	s.sync;
	//load indices to floatarray
	meanfeatures.loadToFloatArray(action: { arg fa; ~meanfeatures = fa;});
	//load indices to floatarray
});
)

~featuresSorted = ~meanfeatures.order;

(
SynthDef(\segPlayer, {
	//var this_idx = Select.kr(\slice.kr(0), ~featuresSorted);
	var this_idx = \slice.kr(0);
	var startsamp = Select.kr(this_idx, ~onsetArr);
	var endsamp = Select.kr(this_idx + 2, ~onsetArr);
	var sig = PlayBuf.ar(2, ~src, BufRateScale.ir(~src) * \rate.kr(1), 0, startsamp, loop: 0);
	var dursecs = (endsamp - startsamp) / BufSampleRate.ir(~src);
	//var env = EnvGen.kr( Env.perc(\atk.kr(0.01), max((\dec.kr(0.1) * dursecs), 0.1), curve: 0), \gate.ar(1), doneAction: Done.freeSelf);
	var env = Env.perc(\atk.kr(0.01), max((\dec.kr(0.1) * dursecs), 0.1), curve: -4).ar(Done.freeSelf);
	sig = sig * env * \gain.ar(0).dbamp;
	//sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\hh, {
	var snd;
	snd = WhiteNoise.ar([0.8, 0.7]);
	snd = BPF.ar(snd, (\freq.kr(1000) + ExpRand(4000, 8000)), 0.8);
	snd = CombL.ar(snd, 0.01, LFNoise1.kr(1).range(0.01, 0.1), 0.01);
	//snd = snd + (GVerb.ar(snd, 4, 30));
	snd = snd * Env.perc(\atk.kr(0.001), \dec.kr(0.1), curve: -4).ar(Done.freeSelf) * \gain.ar(0).dbamp;
	snd = LeakDC.ar(in: snd, coef: 0.995);
	snd = Pan2.ar(snd, \pan.kr(0));
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\kick2, {
	var snd;
	snd = SinOsc.ar(\freq.kr(55) * (1 + (\sweep.kr(2) * Env.perc(0.0, 0.13, curve: -4).ar)) * XLine.ar(1, 0.6 ,1));
	snd = snd + (SinOsc.ar(XLine.ar(4000, 50, 0.01)) * Env.perc(0.0001, 0.01).ar * -5.dbamp);
	snd = snd + (BPF.ar(WhiteNoise.ar, 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	snd = snd + (BPF.ar(Hasher.ar(Sweep.ar), 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	snd = snd * (1 + (4 * Env.perc(0.01, 0.2).ar));
	//snd = snd + (GVerb.ar(snd, \roomsize.kr(8), \reverbtime.kr(1), spread: 16) * -30.dbamp);
	snd = snd * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(4), curve: -3), \gate.ar(1) , doneAction: Done.freeSelf);
	snd = ((snd * 3.dbamp)).tanh;
	snd = snd * \gain.kr(-15).dbamp;
	snd = Pan2.ar(snd, \pan.kr(0));
	snd = LeakDC.ar(in: snd, coef: 0.995);
	Out.ar(\out.kr(0), snd);
}).add;
)

Synth(\segPlayer, [\slice, 1]);

//t.schedAbs(t.nextBar, {t.beatsPerBar_(4)});
t.schedAbs(t.nextBar, {t.tempo_(180/60)});

///COMPOSTION FUNCTIONS
(
//CLOCK
t = TempoClock.new(160/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

//to do
//metric modulation
//accel/decel by phase?
//swing/swing by group
//interpolate between patterns by over x bars
//choke groups
//add risers to hits
//sequence sections
//delta to

~makeSubdivision = { |patt, subdiv|
	var groupCycle = subdiv.asStream.all.size;
	patt = Pn(patt, inf);
	subdiv = subdiv.asInteger;
	subdiv = Pdup(subdiv, subdiv);
	//calculate delta values from subdivision pattern
	Pbind(
		\subdiv, Pn(subdiv , inf),
		\dur, Psubdivide(Pkey(\subdiv), patt),
		\groupCycle, Pn(groupCycle, inf),
	)
};

~makeDeltas = { |patt|
		var eventcount = 0;
		var groupcount = 0;
		//var groupIdxCount
		Pbind(
			//delta time in relation to tempo clock
			\bardelta, (Ptime() % t.beatsPerBar) / t.beatsPerBar,
			//janky ass subroutine
			//group delta time
			\groupdelta, Pfunc({|event|
				var groupdelta;
				//event index in cycle
				event[\eventcount] = eventcount + 1;
				eventcount = eventcount + 1;
				//as delta within group
				groupdelta = (eventcount - 1) / event[\subdiv];
				//index of group
				if(groupcount == event[\groupCycle]) {
					groupcount = 0;
				};
				//set current group count at key
				event[\groupcount] = groupcount + 1;
				//reset event and increment group count
				if(eventcount == event[\subdiv] ) {
					eventcount = 0;
					groupcount = groupcount + 1;
				};
				groupdelta;
			}),
		)
};

~nearestDelta = { |patt, key, downbeat, centre|
	//wtf does this do tbh
	abs(round(centre - key, downbeat) - (centre - downbeat));
};

~filterBeat = { |patt, key, beat|
	var thiskey = key.key;
	Pfunc({ |event| if(beat.includes(event[thiskey])) { event } { event[\instrument] = \rest }});
};
)

// Define a PSeq
//PATTERNS
(
~p = Pseq(#[1, 1, 2], 1);
~s = Pseq(#[5, 5, 5], 1);
//
//\tempo, Pkey(\bardelta).linexp(0, 1, 200/60, 60/60))


Pdef(\hhMod, Pbind(
	\legato, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.25, 0),
	\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.125, 1).max(0.1),
	\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 30, 80),
	//\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0, -0.5).clip(0, 1).ampdb.min(-12),
	\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 2),
	//\sweep, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.1, 3).linexp(0, 1, 8, 0.1),
	\pan, Pkey(\groupdelta).linlin(0, 1, -1, 1),
	//\gain, Pseq(#[-16], inf)
	)
);

Pdef(\kickMod, Pbind(
	\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.125, 1).max(0.1),
	\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 40, 50),
	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0, -0.5).clip(0, 1).ampdb.min(-12),
	\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 2),
	\sweep, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.1, 3).linexp(0, 1, 8, 0.1),
	\pan, Pkey(\groupdelta).linlin(0, 1, -1, 1),
	)
);

//Pseq([10, 100, 50, 20], inf).stutter(8)

Pdef(\sliceMod, Pbind(
	\legato, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.25, 0),
	\atk, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, 1),
	//\gain, ~nearestDelta.(~pattern, Pkey(\bardelta), 0, -0.5).clip(0, 1).ampdb,
	\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linlin(0, 1, 0.5, 0.1),
	\slice, Pkey(\groupdelta).linexp(0, 1, 100, ~onsetArr.size).floor,
	\rate, Pseq([1, 0.5], inf),
	\pan, Pkey(\bardelta).linlin(0, 1, -1, 1),
	\gain, Pseq(#[-16], inf)
	)
);


Pdef(\hhPattern,
	PmonoArtic(\hh) <>
	Pdef(\hhMod) <>
	Pbind(\stretch, Pkey(\bardelta).linlin(0, 1, 0.125, 2)) <>
	//~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[2, 3, 4]) <>
	Plazy{ ~makeDeltas.(~pattern) } <>
	~makeSubdivision.(
		Pseq(#[1, 1, 1], 1),
		Pseq(#[4, 4, 6], 1)
	)
);

Pdef(\kickPattern1,
	PmonoArtic(\kick2) <>
	Pdef(\kickMod) <>
	Pbind(\stretch, Pkey(\bardelta).linlin(0, 1, 0.125, 2)) <>
	~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[4, 1, 2]) <>
	Plazy{ ~makeDeltas.(~pattern) } <>
	~makeSubdivision.(
		Pseq(#[1, 1.5, 1, 1.5, 4], 1),
		Pseq(#[4, 4, 1, 2], 1)
	)
);

Pdef(\slicePattern,
	PmonoArtic(\segPlayer) <>
	//Pbind(\stretch, Pkey(\groupdelta).linexp(0, 1, 0.125, 2)) <>
	Pdef(\sliceMod) <>
	//~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[2, 3]) <>
	Plazy{ ~makeDeltas.(~pattern) } <>
	~makeSubdivision.(
		Pseq(#[1, 1, 1, 1, 1.5, 0.5, 0.5, 1.5], 1),
		Pseq(#[4, 4, 4, 4, 8], 1)
	)
	//Pbind(\instrument, \segPlayer)
);
)

Pdef(\player).stop;

(
Pdef(\player,
	Ppar([
		Pdef(\slicePattern).trace,
		Pdef(\kickPattern1),
		Pdef(\hhPattern)
	]
	)
).play(t, quant:[1,0])
)

~b = Buffer.alloc(s, 2048, 1);

(
Ndef.clear;
Ndef(\fx).stop;
//lfo
Ndef(\lfo, { LFSaw.kr(\lfofreq.kr(0.6)).linlin(0, 1, 0.001, 0.2) });
Ndef(\lfo2, { LFSaw.kr(\lfofreq.kr(0.5)).linlin(0, 1, 0.001, 0.09) });
Ndef(\lfo3, { LFNoise0.kr(\lfofreq.kr(0.25)).linlin(0, 1, 0.01, 0.09)});
//param
Ndef(\fx).proxyspace.quant = 1.0;
Ndef(\fx).fadeTime = 0;
Ndef(\fx).clock = t;
Ndef(\fx)[0]=Pdef(\player);
//verb
Ndef(\fx).xset(\wet1, 0.5);
Ndef(\fx).filter(1 ,{|in| NHHall.ar(in, rt60: 2, stereo: 1, lowFreq: 200, lowRatio: 0.5, hiFreq: 4000, hiRatio: 0.5, earlyDiffusion: 0.5, lateDiffusion: 1, modRate: 0.2, modDepth: 0.2)});
//pitch shift
Ndef(\fx).xset(\wet2, 1);
Ndef(\fx).filter(2, {|in| PitchShift.ar(in, 0.25, \pitch.kr(0.75), \pitchdispersion.kr(0.01), \timedispersion.kr(0.001))});
//spectral compander
Ndef(\fx).filter(3, { arg in, out=0, bufnum=0;
    var chain = FFT(~b.bufnum, in);
	// chain = PV_Compander(chain, \companderThresh.kr(0) * 100, 0.5, 1.0);
    Out.ar(0, Limiter.ar(IFFT(chain), 0.999, 0.05).dup);
});
//lfo mappings
Ndef(\fx).map(\timedispersion, Ndef(\lfo), \pan, Ndef(\lfo2), \pitchdispersion, Ndef(\lfo3), \companderThresh, Ndef(\lfo2), \wet1, Ndef(\lfo));
Ndef(\fx).play;
)
//Pfx(Pdef(\kickPattern1),\delay).play(t, quant:2);