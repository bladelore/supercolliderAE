(
//Server.default.options.device = "headphonesSc";
s.options.blockSize = 1;
s.options.memSize = 8192 * 16;
s.reboot;
)
s.quit;

FluidWaveform(~src, ~indices);

(
//var file = "/Users/asherelazary/Desktop/Samples etc./NEW sample lib/backyard oct 11 2018.wav";
//var file = "/Users/asherelazary/Desktop/Samples etc./Film Sounds/102060__juskiddink__finnish-sauna.wav";
var file ="/Users/asherelazary/Desktop/Samples etc./NEW sample lib/207241__matxuketeam__hit-metalic-bin.wav";
//var file = "/Users/asherelazary/Desktop/Samples etc./NEW sample lib/Jungle riddims/jungle warrior.wav";
//var file = "/Users/asherelazary/Desktop/Samples etc./NEW sample lib/mother and daughter singing o magnum mysterium-sfLDOVcK7nU.wav";
//var file = "/Users/asherelazary/Desktop/Samples etc./hollywood edge - foley sound library/FSL-05/FSL-05-Cards Out Of Shoe Steady; Card Out Of Shoe And Slide With Light Click Steady. - Plastic Clicks.wav";
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

(
SynthDef(\fb1, {
	var snd, impulse, fb, modEnv1, del2;
	var time = \time.kr(0.1) - ControlRate.ir.reciprocal;
	var damp = (1 - \damp.kr(0.5));

	fb = LocalIn.ar(1) * \feedback.kr(0.5);

	modEnv1 = XLine(1, 0.5, \dec.kr(0.1)).poll;

	impulse = Dust.ar(\impulse.kr(44100));
	impulse = HairCell.ar(impulse, spontaneousrate: \spont.kr(1), boostrate: \boost.kr(2000), restorerate: (1-fb) * \restore.kr(100), loss: 1 - fb);
	impulse = OnePole.ar(impulse, damp);

	snd = DelayC.ar(fb, 2, time);
	snd = snd + fb + impulse;
	snd = RLPFD.ar(snd, ffreq: \filter.kr(440), res: 0, dist: 0);
	snd = (snd * \dist.kr(36).dbamp).tanh;
	del2 = AllpassC.ar(snd, 2, time);
	del2 = NestedAllpassC.ar(del2, maxdelay1: 0.036, delay1: time, gain1: 0.08, maxdelay2: 0.03, delay2: time * 2, gain2: 0.3, mul: 1.0, add: 0.0);
	snd = SelectX.ar(\delay2.kr(0.8),[snd, del2]);
	snd = SelectX.ar(\exciter.kr(0.0),[snd, impulse]);
	snd = OnePole.ar(snd, damp);
	snd = LeakDC.ar(in: snd, coef: 0.995);

	snd = snd ! 2;
	snd = snd + (NHHall.ar(snd, rt60: \rev.kr(0.1)) * -5.dbamp);
	LocalOut.ar(snd.sum);
	//DetectSilence.ar(snd, doneAction: Done.freeSelf);
	snd = snd * EnvGen.kr(Env.perc(\atk.kr(0.01), \dec.kr(0.1), curve: -8), \gate.kr(1), doneAction: Done.freeSelf);
	snd = snd * \gain.kr(0).dbamp;
	snd = snd.sanitize;
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\hh, {|gate=1|
	var snd;
	snd = WhiteNoise.ar([0.8, 0.7]);
	snd = BPF.ar(snd, (\freq.kr(1000) + ExpRand(4000, 8000)), 0.8);
	snd = CombL.ar(snd, 0.01, LFNoise1.kr(1).range(0.01, 0.1), 0.01);
	snd = snd * Env.perc(\atk.kr(0.001), \dec.kr(0.1), curve: -4).ar(Done.freeSelf);
//	snd = snd * EnvGen.kr(Env.perc(\atk.kr(0.001), \dec.kr(0.5), curve: -4), gate , doneAction: Done.freeSelf);
	//snd = snd * EnvGen.kr(Env.adsr(\atk.kr(0.04), \dec.kr(0.2), 0.6, 0.1), t_gate, doneAction: Done.freeSelf);
	snd = snd * \gain.kr(0).dbamp;
	snd = LeakDC.ar(in: snd, coef: 0.995);
	snd = Pan2.ar(snd, \pan.kr(0));
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\kick2, {|gate=1|
	var snd;
	snd = SinOsc.ar(\freq.kr(55) * (1 + (\sweep.kr(2) * Env.perc(0.0, 0.13, curve: -4).ar)) * XLine.ar(1, 0.6 ,1));
	snd = snd + (SinOsc.ar(XLine.ar(4000, 50, 0.01)) * Env.perc(0.0001, 0.01).ar * -5.dbamp);
	snd = snd + (BPF.ar(WhiteNoise.ar, 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	snd = snd + (BPF.ar(Hasher.ar(Sweep.ar), 10120, 0.3) * Env.perc(0.001, 0.03).ar * -8.dbamp);
	snd = snd * (1 + (4 * Env.perc(0.01, 0.2).ar));
	//snd = snd + (GVerb.ar(snd, \roomsize.kr(8), \reverbtime.kr(1), spread: 16) * -30.dbamp);
	snd = snd * EnvGen.kr(Env.perc(\atk.kr(0.04), \dec.kr(0.2), 0.6, 0.1), gate, doneAction: Done.freeSelf) * \gain.kr(-15).dbamp;
	//EnvGen.kr(Env.adsr(0.04, 0.2, 0.6, 0.1), gate, doneAction: Done.freeSelf)
	snd = ((snd * 10.dbamp)).tanh;
	//snd = snd * \gain.kr(-15).dbamp;
	snd = Pan2.ar(snd, \pan.kr(0));
	snd = LeakDC.ar(in: snd, coef: 0.995);
	Out.ar(\out.kr(0), snd);
}).add;

SynthDef(\grainSlicer, { |bufnum, gate=1|
	var polarity, polarityProb, pan, grains, env;
	var bufFrames = BufFrames.ir(bufnum);
	var t = Impulse.ar( \trigRate.kr(100));
	var dur = \trigRate.kr(100).reciprocal * \overlap.kr(1) * \rate.kr(1).reciprocal;

	var this_idx = Select.kr(\slice.kr(0), ~featuresSorted);
	var startsamp = (Select.kr(this_idx, ~onsetArr) / bufFrames);
	var endsamp = (Select.kr(this_idx + 1, ~onsetArr) / bufFrames);
	var sampsDur = endsamp - startsamp;

	var granPhasor = Phasor.ar(
		rate: (1 / bufFrames * BufRateScale.kr(bufnum)) * \posRate.kr(0.5),
		start: startsamp,
		end: endsamp,
		//trig: gate,
		resetPos: startsamp
	);

	granPhasor = Wrap.ar(granPhasor + (\pos.kr(0) * sampsDur), startsamp, endsamp);

	polarityProb = Drand([-1 + Diwhite(0, \polarityMod.kr(1), inf), 1], inf) * -1;
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
		interp: 2
	);

	grains = grains * EnvGen.kr(Env.adsr(\atk.kr(0.04), 0.2, 0.6, \dec.kr(0.2)), gate, doneAction: Done.freeSelf);
	grains = Balance2.ar(grains[0], grains[1], \pan_master.kr(0)) * \gain.kr(0).dbamp;
	Out.ar(\out.kr(0), grains);

}).add;

)

Synth(\grainSlicer, [\bufnum, ~src.bufnum, \slice, 2, \overlap, 20, \posRate, 1, \trigRate, 1000])

// Define a PSeq
//PATTERNS
(
~p = Pseq(#[1, 1, 2], 1);
~s = Pseq(#[5, 5, 5], 1);
//
//\tempo, Pkey(\bardelta).linexp(0, 1, 200/60, 60/60))

Pdef(\hhMod, Pbind(
	\legato, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.25, 0),
	\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.5, 0.75) * 0.125,
	\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 30, 80),
	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.25, 0.5).clip(0, 1).ampdb.min(-12),
//	\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 0.4),
	\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.33, 0) * 0.125,
	\pan, Pkey(\groupdelta).linlin(0, 1, -1, 1),
	\gain, Pseq(#[-16], inf)
	)
);

Pdef(\kickMod, Pbind(
	\legato, ~nearestDelta.(~pattern, Pkey(\bardelta), 0.25, 0).max(0.5),
	//\atk, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.125, 1).linexp(0, 1, 0.1, 0.3),
	\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 40, 50),
	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.125, 0).clip(0, 1).ampdb.min(-12),
	\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 2),
	\sweep, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.1, 3).linexp(0, 1, 3, 0.1),
	\pan, Pkey(\groupdelta).linlin(0, 1, -0.3, 0.3),
	)
);

Pdef(\grainMod, Pbind(
	\legato, ~nearestDelta.(~pattern, Pkey(\bardelta), 0.25, 0).max(0.5),
	\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.125, 1).linexp(0, 1, 0.1, 0.3),
	\posRate, Pkey(\groupdelta).linlin(0, 1, 0.01, 0.5),
	\polarityMod, Pwhite(1, 1, inf),
	\overlap, Pwhite(0.4, 20, inf),
	\panMod, Pwhite(0, Pkey(\bardelta), inf),
	\bufnum, ~src.bufnum,
	\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linlin(0, 1, 0, 4),
	//\pos, Pwhite(0, 0.7, inf).stutter(4),
	\slice, Pkey(\groupdelta).linlin(0, 1, 0 + Pkey(\groupcount), ~onsetArr.size + Pkey(\groupcount)).floor,
	\slice, Pn(Pseries(0, 1, ~onsetArr.size) + 100, inf).stutter(2),
	//\slice, 100,
	\trigRate, Pkey(\groupdelta).linlin(0, 1, 30, 1000),
	//\gain, ~nearestDelta.(~pattern, Pkey(\bardelta), 0, -0.5).clip(0, 1).ampdb,
	\gain, -6,
	\rate, 1
	)
);



Pdef(\hhPattern,
	PmonoArtic(\hh) <>
	Pdef(\hhMod) <>
	//Pbind(\stretch, Pkey(\bardelta).linlin(0, 1, 0.125, 2)) <>
	//~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3, 5, 7]) <>
	~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 4]) <>
	Plazy{ ~makeDeltas.(~pattern) } <>
	~makeSubdivision.(
		Pseq(#[4, 2, 0.75], 1),
		//Pseq(#[2, 4, 4, 8] * 0.5, 1),
		//Pseq(#[1, 1, 1, 1], 1),
		Pseq(#[4, 4, 4, 4] * 2, 1)
	)
);

Pdef(\kickPattern1,
	PmonoArtic(\kick2) <>
	Pdef(\kickMod) <>
	Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 2, 0.125)) <>
	~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 4, 5, 7]) <>
	~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 2, 4]) <>
	Plazy{ ~makeDeltas.(~pattern) } <>
	~makeSubdivision.(
		Pseq(#[4, 3, 1, 2, 3, 0.75], 1),
		Pseq(#[2, 4, 4], 1)
	)
);

Pdef(\grainPattern,
	PmonoArtic(\grainSlicer) <>
	Pbind(\stretch, Pkey(\groupdelta).linexp(0, 1, 0.125, 4)) <>
	Pdef(\grainMod) <>
	~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1]) <>
	//~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[3]) <>
	Plazy{ ~makeDeltas.(~pattern) } <>
	~makeSubdivision.(
		Pseq(#[4, 3, 1, 2, 3, 0.75], 1),
		//Pseq(#[1, 1, 1, 1], 1),
		Pseq(#[4, 4, 4, 4], 1)
	)
	<> Pbind(\instrument, \grainSlicer)
);

Pdef(\fb1Mod, Pbind(
	//\legato, ~nearestDelta.(~pattern, Pkey(\bardelta), 0.25, 0).max(0.5),
	//\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.125, 1).linexp(0, 1, 0.1, 0.3),
	\dec, Pkey(\dur) * 2,
	\feedback, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 1) + Pwhite(0.1, 0.5),
	\time, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 0.1, 1),
	//\time, 0.018,
	//\damp, Pkey(\groupdelta).linlin(1, 0, 0.5, 1),
	//\damp, 1,
	\impulse, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 10000),
	\filter, Pkey(\groupdelta).linexp(0, 1, 20000, 5000),
	\exciter, Pkey(\groupdelta).linexp(0, 1, 0, 0.5),
	\delay2, Pkey(\groupdelta).linlin(0, 1, 0, 1),
	\exciter, Pkey(\groupdelta).linlin(0, 1, 0, 1),
	\spont, Pkey(\groupdelta).linlin(0, 1, 2646.37, 20000) + Pwhite(10, 20),
	//\boost, Pkey(\groupdelta).linlin(1, 0, 1, 3000) + Pwhite(10, 20),
	//\restore, Pkey(\groupdelta).linlin(1, 0, 100, 1) + Pwhite(10, 20),
	\dist, Pkey(\groupdelta).linlin(0, 1, 32, 64),
	\rev, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.125, 1).linexp(0, 1, 1, 2),
	\res, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.125, 1).linexp(0, 1, 0.5, 32),
	//\pos, Pwhite(0, 0.7, inf).stutter(4),
	//\slice, 100,
	\gain, 0,
	)
);

Pdef(\fb1Pattern,
	PmonoArtic(\fb1) <>
	Pdef(\fb1Mod) <>
	Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 2, 0.125)) <>
	~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 4, 5, 7]) <>
	~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 2, 4]) <>
	Plazy{ ~makeDeltas.(~pattern) } <>
	~makeSubdivision.(
		Pseq(#[1.5, 1, 1, 0.5], 1),
		Pseq(#[2, 4, 4], 1)
	)
);



)

Pdef(\player).stop;

(
Pdef(\player,
	Ppar([
		Pdef(\fb1Pattern),
		Pdef(\kickPattern1),
		//Pdef(\hhPattern),
		//Pdef(\grainPattern),
	]
	)
)
)

Pdef(\player).play(t, quant:[1,0]);

Pdef(\fb1Pattern).play(t, quant:[1,0]);
// Pbind(\instrument, \kick2, \dur, Pseq([1], inf)).play(t, quant:[1,0]);

Pdef(\hhPattern).play(t, quant:[1,0]);
Pdef(\kickPattern1).play(t, quant:[1,0]);
Pdef(\grainPattern).play(t, quant:[1,0]);
Pdef(\grainPattern).stop;
Pdef(\player).stop;

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
Ndef(\fx).ar(2);
//lfo
Ndef(\lfo, { LFSaw.kr(\lfofreq.kr(1.2)).linlin(0, 1, 0.001, 0.2) });
Ndef(\lfo2, { LFSaw.kr(\lfofreq.kr(2)).linlin(0, 1, 0.001, 0.09) });
Ndef(\lfo3, { LFNoise0.kr(\lfofreq.kr(0.5)).linlin(0, 1, 0.01, 0.09)});
Ndef(\lfo4, { LFNoise1.kr(\lfofreq.kr(1/8)).linlin(0, 1, 0.25, 1)});
//param
Ndef(\fx).proxyspace.quant = 1.0;
Ndef(\fx).fadeTime = 0;
Ndef(\fx).clock = t;
Ndef(\fx)[0]=Pdef(\player);
//verb
Ndef(\fx).xset(\wet1, 0.5);
Ndef(\fx).filter(1, {|in| NHHall.ar(in, rt60: 0.1, stereo: 1, lowFreq: 200, lowRatio: 0.5, hiFreq: 4000, hiRatio: 0.5, earlyDiffusion: 0.5, lateDiffusion: 1, modRate: 0.2, modDepth: 0.2) });
//pitch shift
Ndef(\fx).set(\wet2, 1);
Ndef(\fx).filter(2, {|in| PitchShift.ar(in, 0.25, \pitch.kr(0.75), \pitchdispersion.kr(0.5), \timedispersion.kr(0.001))});
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
//Ndef(\fx).filter(10, {|in| ~compressor.(in, 0.01, 0.05, -6, 4)});
//lfo mappings
Ndef(\fx).map(\timedispersion, Ndef(\lfo), \pitchdispersion, Ndef(\lfo3), \companderThresh, Ndef(\lfo2));
//Ndef(\fx).map(\wet1, Ndef(\lfo4), \wet2, Ndef(\lfo4));
Ndef(\fx).play(fadeTime:0);
)

//Pfx(Pdef(\kickPattern1),\delay).play(t, quant:2);