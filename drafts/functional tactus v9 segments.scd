(
//Server.default.options.device = "headphonesSc";
s.boot;
)
s.quit;
~grainbuf = Buffer.readChannel(s, "/Users/asherelazary/Desktop/Samples etc./NEW sample lib/02seraingcentrauxchaines29109h15.mp3", channels:[0]);
//~grainbuf = Buffer.readChannel(s, "/Users/asherelazary/Desktop/Samples etc./NEW sample lib/Jungle riddims/jungle warrior.wav", channels:[0]);

FluidWaveform(~src, ~indices);

(
//var file = "/Users/asherelazary/Desktop/Samples etc./NEW sample lib/backyard oct 11 2018.wav";
//var file = "/Users/asherelazary/Desktop/Samples etc./Film Sounds/102060__juskiddink__finnish-sauna.wav";
//var file ="/Users/asherelazary/Desktop/Samples etc./NEW sample lib/207241__matxuketeam__hit-metalic-bin.wav";
var file = "/Users/asherelazary/Desktop/Samples etc./Field recs/Rock song.wav";
//analyse mono
~src = Buffer.readChannel(s, file, channels: [0]);
//play stereo or source
~playSrc = Buffer.read(s, file);
//make indices buffer
~indices = Buffer(s);
~meanfeatures = Buffer(s);
~spec = Buffer(s);
~stats = Buffer(s);
//nrt onset slice of source
FluidBufOnsetSlice.processBlocking(s, ~src, metric: 9, threshold: 0.1, indices: ~indices, action: {"found slices".postln});
//FluidBufNoveltySlice.processBlocking(s, ~src, indices: ~indices, action: {"found slices".postln});
)

(
~indices.loadToFloatArray(action: {
	arg fa;
	//iterate through adjacent pairs of indices (tuple like)
	fa.doAdjacentPairs{
		arg start, end, i;
		var numSamps = end - start;
		//compute spectral features per fft frame (centroid only)
		FluidBufSpectralShape.processBlocking(s, ~src, start, numSamps, features: ~spec, select:[\centroid]);
		//buf stats channels: mean std skew kurtosis min median max
		//select mean
		FluidBufStats.processBlocking(s, ~spec, stats: ~stats, select:[\mean]);
		FluidBufCompose.processBlocking(s, ~stats, destination: ~meanfeatures, destStartFrame: i);
	};
	//get indices
	~onsetArr = fa;
	//get selected features
	~meanfeatures.loadToFloatArray(action: { arg fa; ~meanfeatures = fa;});
	//sort selected feature
	~featuresSorted = ~meanfeatures.order;
	"done analysis".postln;
});
)

(
SynthDef(\segPlayer, {
	//slice index
	//var this_idx = \slice.kr(0);
	var this_idx = Select.kr(\slice.kr(0), ~featuresSorted);
	var startsamp = Select.kr(this_idx, ~onsetArr);
	var endsamp = Select.kr(this_idx + 1, ~onsetArr);
	//channels and sampler
	var numChans = ~playSrc.numChannels;
	var sig = PlayBuf.ar(numChans, ~playSrc, BufRateScale.ir(~playSrc) * \rate.kr(1), 0, startsamp, loop: 0);
	//handle channels
	var panned = case
	{numChans == 1} {Pan2.ar(sig, \pan.kr(0))}
	{numChans == 2} {Balance2.ar(sig[0], sig[1], \pan.kr(0))}
	{numChans > 2} { var splay = Splay.ar(sig); Balance2.ar(splay[0], splay[1], \pan.kr(0)); } ;
	//envelope
	var dursecs = (endsamp - startsamp) / BufSampleRate.ir(~playSrc);
	var env = Env.perc(\atk.kr(0.01), (\dec.kr(0.1) * dursecs), curve: 0).ar(Done.freeSelf);
	//out
	sig = panned * env * \gain.ar(0).dbamp;
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
	snd = ((snd * 10.dbamp)).tanh;
	snd = snd * \gain.kr(-15).dbamp;
	snd = Pan2.ar(snd, \pan.kr(0));
	snd = LeakDC.ar(in: snd, coef: 0.995);
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\granulator, { |bufnum, gate=1|
	var polarity, polarityProb, pan, grains;
	var bufFrames = BufFrames.ir(bufnum);
	var t = Impulse.ar(\trigRate.kr(20));
	var dur = \trigRate.kr(20).reciprocal * \overlap.kr(1) * \rate.kr(1).reciprocal;

	var granPhasor = Phasor.ar(
		rate: (1 / bufFrames * BufRateScale.kr(bufnum)) * \posRate.kr(0.5),
		start: 0.0,
		end: 1,
		trig: gate,
		resetPos: 0.0
	);

	granPhasor = Wrap.ar(granPhasor + \pos.kr(0), 0, 1);

	polarityProb = Drand([-1 + Diwhite(0, \polarityMod.kr(0), inf), 1], inf) * -1;
	polarityProb = (polarityProb * 2 - 1);
	polarity = Demand.ar(t, 0, polarityProb);

	pan = Demand.ar(t, 0, Drand([-1, 0, 1] * \panMod.kr(1), inf));

	grains = TGrains.ar(
		numChannels: 2,
		trigger: t,
		bufnum: bufnum,
		rate: \rate.kr(1),
		centerPos: (granPhasor * BufDur.kr(bufnum)) + (dur * 0.5001),
		dur: dur,
		pan: pan,
		amp: polarity,
		interp: 4
	);

	grains = grains * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(0.1), curve: -8), gate, doneAction: Done.freeSelf);
	grains = Balance2.ar(grains[0], grains[1], \pan_master.kr(0));
	Out.ar(\out.kr(0), grains);

}).add;

)

//t.schedAbs(t.nextBar, {t.beatsPerBar_(4)});
t.schedAbs(t.nextBar, {t.tempo_(140/60)});

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
	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0, -0.5).clip(0, 1) - 12,
	\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 2),
	\pan, Pkey(\groupdelta).linlin(0, 1, -1, 1),
	//\gain, Pseq(#[-16], inf)
	)
);

Pdef(\kickMod, Pbind(
	\legato, ~nearestDelta.(~pattern, Pkey(\bardelta), 0.25, 0).max(0.5),
	//\atk, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.125, 1).linexp(0, 1, 0.1, 0.3),
	\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 40, 50),
	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.125, 0).clip(0, 1).ampdb.min(-12),
	\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 2),
	\sweep, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.1, 3).linexp(0, 1, 3, 0.1),
	//\pan, Pkey(\groupdelta).linlin(0, 1, -1, 1),
	)
);

//Pseq([10, 100, 50, 20], inf).stutter(8)

Pdef(\sliceMod, Pbind(
	//\legato, ~nearestDelta.(~pattern, Pkey(\bardelta), 0.25, 0).max(0.5),
	\atk, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, 1),
	\gain, ~nearestDelta.(~pattern, Pkey(\bardelta), 0, -0.5).clip(0, 1).ampdb,
	\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linlin(0, 1, 0.5, 0.1),
	\slice, (Pkey(\groupdelta).linexp(0, 1, 0, 200).floor.asInteger + Pkey(\bardelta).linexp(0, 1, 100, 200).floor.asInteger),
	//\slice, Pn(Pseries(200, 1, 100), inf).stutter(8),
	\rate, Pseq([1, 0.5], inf).stutter(2),
	\pan, Pkey(\bardelta).linlin(0, 1, -1, 1),
	//\gain, Pseq(#[-16], inf)
	)
);

Pdef(\grainMod, Pbind(
	\posrate, Pkey(\groupdelta).linexp(0, 1, 0.01, 1),
	\polarityMod, Pwhite(1, 1, inf),
	\overlap, Pwhite(0.5, 20, inf),
	\panMod, Pwhite(0, 1, inf),
	\bufnum, ~grainbuf.bufnum,
	\dec, Pwhite(1, 0.4, inf),
	\pos, Pwhite(0, 0.7, inf).stutter(4),
	\trigRate, Pkey(\bardelta).linexp(0, 1, 200, 1000),
	\rate, 0.5
	)
);

Pdef(\hhPattern,
	PmonoArtic(\hh) <>
	Pdef(\hhMod) <>
	Pbind(\stretch, Pkey(\bardelta).linlin(0, 1, 0.125, 2)) <>
	~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3, 5, 7]) <>
	//~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 4]) <>
	Plazy{ ~makeDeltas.(~pattern) } <>
	~makeSubdivision.(
		Pseq(#[1, 1, 1, 0.5], 1),
		Pseq(#[4, 4, 6], 1)
	)
);

Pdef(\kickPattern1,
	PmonoArtic(\kick2) <>
	Pdef(\kickMod) <>
	//Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 2, 0.125)) <>
	~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 4, 5, 7]) <>
	~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 4]) <>
	Plazy{ ~makeDeltas.(~pattern) } <>
	~makeSubdivision.(
		Pseq(#[4, 3, 1, 2, 3, 0.75], 1),
		Pseq(#[2, 4, 4, 8], 1)
	)
);

Pdef(\grainPattern,
	//PmonoArtic(\granulator) <>
	//Pbind(\stretch, Pkey(\groupdelta).linexp(0, 1, 0.125, 2)) <>
	Pdef(\grainMod) <>
	//~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 2, 4]) <>
	Plazy{ ~makeDeltas.(~pattern) } <>
	~makeSubdivision.(
		Pseq(#[4, 3, 1, 2, 3, 0.75], 1),
		Pseq(#[2, 4, 4, 8], 1)
	)
	<> Pbind(\instrument, \granulator)
);

Pdef(\slicePattern,
	//PmonoArtic(\segPlayer) <>
	//Pbind(\stretch, Pkey(\groupdelta).linexp(0, 1, 0.125, 2)) <>
	Pdef(\sliceMod) <>
	//~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 2, 4]) <>
	Plazy{ ~makeDeltas.(~pattern) } <>
	~makeSubdivision.(
		Pseq(#[1, 1.5, 0.5, 1.5, 1, 0.75], 1),
		Pseq(#[4, 4, 4, 8], 1)
	)
	<> Pbind(\instrument, \segPlayer)
);
)



Pdef(\player).stop;

(
Pdef(\player,
	Ppar([
		//Pdef(\slicePattern),
		Pdef(\kickPattern1),
		Pdef(\hhPattern),
		Pdef(\grainPattern),
	]
	)
)
)

Pdef(\grainPattern).play(t, quant:[1,0]);
Pdef(\player).play(t, quant:[1,0]);
Synth(\granulator, [bufnum: ~grainbuf.bufnum]);

~b = Buffer.alloc(s, 2048, 1);

(
~compressor = { |snd, attack, release, threshold, ratio|
	var amplitudeDb, gainDb;
	amplitudeDb = Amplitude.ar(snd, attack, release).ampdb.mean;
	gainDb = ((amplitudeDb - threshold) * (1 / ratio - 1)).min(0);
	snd * gainDb.dbamp;
};
)

(
Ndef(\fx).stop;
Ndef.clear;
//lfo
Ndef(\lfo, { LFSaw.kr(\lfofreq.kr(0.6)).linlin(0, 1, 0.001, 0.2) });
Ndef(\lfo2, { LFSaw.kr(\lfofreq.kr(0.5)).linlin(0, 1, 0.001, 0.09) });
Ndef(\lfo3, { LFNoise0.kr(\lfofreq.kr(0.25)).linlin(0, 1, 0.01, 0.09)});
Ndef(\lfo4, { LFNoise1.kr(\lfofreq.kr(1/8)).linlin(0, 1, 0.25, 1)});

//param
Ndef(\fx).proxyspace.quant = 1.0;
Ndef(\fx).fadeTime = 0.1;
Ndef(\fx).clock = t;
Ndef(\fx)[0]=Pdef(\player);
//verb
Ndef(\fx).xset(\wet1, 0.5);
Ndef(\fx).filter(1, {|in| NHHall.ar(in, rt60: 2, stereo: 1, lowFreq: 200, lowRatio: 0.5, hiFreq: 4000, hiRatio: 0.5, earlyDiffusion: 0.5, lateDiffusion: 1, modRate: 0.2, modDepth: 0.2) });
//pitch shift
Ndef(\fx).set(\wet2, 1);
Ndef(\fx).filter(2, {|in| PitchShift.ar(in, 0.25, \pitch.kr(0.75), \pitchdispersion.kr(0.01), \timedispersion.kr(0.001))});
//spectral compander
Ndef(\fx).set(\wet3, 0.5);
Ndef(\fx).filter(3, { arg in, out=0, bufnum=0;
	var chain = FFT(~b.bufnum, in);
	chain = PV_Compander(chain, \companderThresh.kr(0) * 100, 2, 1.0);
	//chain = PV_MagSmear(chain, MouseX.kr(0, 100));
	//chain = PV_Diffuser(chain, MouseY.kr > 0.5 );
	Out.ar(0, Limiter.ar(IFFT(chain), 0.999, 0.05).dup);
});
//compressor
Ndef(\fx).filter(10, {|in| ~compressor.(in, 0.01, 0.05, -6, 4)});
//lfo mappings
Ndef(\fx).map(\timedispersion, Ndef(\lfo), \pitchdispersion, Ndef(\lfo3), \companderThresh, Ndef(\lfo2));
Ndef(\fx).map(\wet1, Ndef(\lfo4), \wet2, Ndef(\lfo4));
Ndef(\fx).play(fadeTime:0);
)

//Pfx(Pdef(\kickPattern1),\delay).play(t, quant:2);