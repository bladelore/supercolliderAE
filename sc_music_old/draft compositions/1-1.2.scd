t.schedAbs(t.nextBar, {t.tempo_(170/60)});
Pdef.clear;

(

Pdef(\hhMod, Pbind(
	\legato, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.25, 0),
	\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.5, 0.75) * 0.125,
	\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 30, 80),
	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.25, 0.5).clip(0, 1).ampdb.min(-12),
	\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.33, 0) * 0.125,
	\pan, Pkey(\groupdelta).linlin(0, 1, -1, 1),
	\gain, Pseq(#[-16], inf)
	)
);

Pdef(\kickMod, Pbind(
	\legato, ~nearestDelta.(~pattern, Pkey(\bardelta), 0.25, 0).max(0.5),
	//\atk, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.125, 1).linexp(0, 1, 0.1, 0.3),
	\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 40, 50),
	//\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.125, 0).clip(0, 1).ampdb.min(-12) + dbamp(6),
	\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 2),
	\sweep, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.1, 3).linexp(0, 1, 8, 0.1),
	\pan, Pkey(\groupdelta).linlin(0, 1, -0.3, 0.3),
	\gain, -3
	)
);

Pdef(\hhPattern,
	PmonoArtic(\hh) <>
	Pdef(\hhMod) <>
	Pbind(\stretch, Pkey(\bardelta).linlin(0, 1, 0.125, 2)) <>
	~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3, 5, 7]) <>
	~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 4]) <>
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
	~makeSubdivision.(
		//Pseq(#[1, 1, 1, 1,], 1),
		//Pseq(#[2, 2, 2, 10], 1),
		Pseq(#[4, 3, 1, 2, 3, 0.75, 1], 1),
		Pseq(#[2, 4, 4], 1)
	)
);

/*Pdef(\grainMod, Pbind(
	\bufnum, ~src.bufnum,
	\dec, Pkey(\dur) * 0.5,
	//\slice, ~pmodenv.(Pwhite(200, 150,inf), Pseq([1,1],inf)),
	\slice, Pkey(\eventcount) + 104,
	//\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.125, 1).linexp(0, 1, 0.1, 0.3),
	\posRate, ~pmodenv.(Pseq([0.01,1],inf), Pseq([1,1],inf)),
	\polarityMod, 1,
	\overlap, ~pmodenv.(Pseq([1,40],inf), Pseq([4,4],inf)),
	\panMod, Pwhite(0, Pkey(\bardelta), inf),
	\pos, ~pmodenv.(Pseq([0,0.5,1,0.5],inf), Pseq([1,2],inf)),
	\trigRate, ~pmodenv.(Pseq([100,2000,100,400],inf), Pseq([0.5,1]/4,inf)),
	\pan_master, ~pmodenv.(Pseq([1, 0, -1],inf), Pseq([2,2],inf)),
	\gain, 0,
	\rate, 1
	)
);*/

Pdef(\grainMod, Pbind(
	//\instrument, Prand([\hh, \kick2, \grainSlicer], inf),
	//\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 40, 50),

	\bufnum, ~src.bufnum,
	\dec, Pkey(\dur) * 0.5,
	//\slice, ~pmodenv.(Pwhite(200, 150,inf), Pseq([1,1],inf)),
	\slice, Pkey(\cyclecount).stutter(Pwrand([1, 4, 2], [2, 1, 0.5].normalizeSum, inf)) + 100,
	\posRate, ~pmodenv.(Pkey(\groupdelta), Pkey(\dec)),
	\polarityMod, 0,
	\overlap, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 4, 20, -4), Pkey(\dec)),
	\panMod, Pwhite(0, Pkey(\bardelta), inf),
	\trigRate, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 200, 4000, -4), Pkey(\dec)),
	\pan_master, ~pmodenv.(Pseq([1, 0, -1],inf), Pkey(\dec)),
	//\gain, -16,
	\rate, 2
	)
);

Pdef(\grainPattern,
	//PmonoArtic(\grainSlicer) <>
	//Pbind(\stretch, Pkey(\groupdelta).linexp(0, 1, 0.125, 4)) <>
	Pdef(\grainMod) <>
	//~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3]) <>
	//~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[3, 5]) <>
	~makeSubdivision.(
		Pseq(#[1, 1, 1, 1,], 1),
		Pseq(#[4, 4, 4, 4], 1)
	)
	<> Pbind(\instrument, \grainSlicer)
);

Pdef(\fb1Mod, Pbind(
	\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 40, 50),
	//\legato, ~nearestDelta.(~pattern, Pkey(\bardelta), 0.25, 0).max(0.5),
	//\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.125, 1).linexp(0, 1, 0.1, 0.3),
	//\dec, ~pmodenv.(Pseq([1, 0.5, 0.25],inf), Pseq([2,1,0.5],inf)),
	//\dec, Pkey(\dur) * 1,
	\dec, Pkey(\dur) * 0.5,
	//\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 2) * 1,
	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.125, 0).clip(0, 1).ampdb + dbamp(6),
//	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0, 0.5).clip(0, 1).ampdb,
	\sweep, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 2, 3, -4), Pkey(\dec)),
	\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pseq([4,8,2,4],inf)),
	\time, ~pmodenv.(Pseq([0.005, 0.001, 0.010] * 100,inf), Pseq([1, 4, 2],inf), Pseq([2, 1, 3, inf]), Pwhite(-1, 1, inf)),
	//\time, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 0.01, 0.05, -8), Pkey(\dec), 1, Pseq([\step, \sine, -3],inf)),
	\res, ~pmodenv.(Pseq([1, 0],inf), Pseq([0.25, 0.25], inf)),
	//\res, 0,
	//\damp, ~pmodenv.(Pseq([0.1, 1],inf), Pwhite(0.01, 0.4, inf)),
	\damp, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 0.1, 1, -4), Pkey(\dec)),
	//\damp, 0.5,
	//\filter, 100,
	\filter, ~pmodenv.(Pkey(\groupdelta).lincurve(0,1,2000,500,-4), Pkey(\dec)),
	//\filter, ~pmodenv.(Pseq([60, 30],inf), Pseq([1, 1, 2], inf)),
	//\filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),
	\delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5,0.25], inf)),
	//\delay2, 0.5,
	//\exciter, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5,0.25], inf)),
	\exciter, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 0, 1, -8), Pkey(\dur), 1, Pseq([\sine],inf)),
	// \exciter, 0,
	\impulse, ~pmodenv.(Pwhite(20000,200,inf), Pseq([1], inf)),
	\spont, ~pmodenv.(Pseq([1,30],inf), Pseq([4,4], inf)),
	\boost,  ~pmodenv.(Pseq([20000,200],inf), Pseq([1], inf)),
	\restore, ~pmodenv.(Pseq([1000,20000],inf), Pseq([0.5, 0.75],inf)),
	\dist,  ~pmodenv.(Pseq([16, 32] * 2,inf), Pseq([4, 4, 4],inf)),
	//\dist, 64,
	\rev, ~pmodenv.(Pexprand(0.1, 2, inf), Pseq([2, 4],inf)),
	\pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pseq([0.5, 0.25],inf)),
	//\gain, 0,
	\bufnum, ~src.bufnum,

	//\slice, ~pmodenv.(Pwhite(200, 150,inf), Pseq([1,1],inf)),
	\slice, Pkey(\cyclecount).stutter(Pwrand([1, 4, 2], [2, 1, 0.5].normalizeSum, inf)) + 100,
	\posRate, ~pmodenv.(Pkey(\groupdelta), Pkey(\dec)),
	\polarityMod, 1,
	\overlap, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 4, 20, -4), Pkey(\dec)),
	\panMod, Pwhite(0, Pkey(\bardelta), inf),
	\trigRate, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 200, 4000, -4), Pkey(\dec)),
	\pan_master, ~pmodenv.(Pseq([1, 0, -1],inf), Pkey(\dec)),
	//\gain, -16,
	\rate, 2

	)
);

Pdef(\fb1Pattern,
	//PmonoArtic(\fb1) <>
//	Pfx(\echo, \dtime, 0.2, \decay, 3)
	Pdef(\fb1Mod) <>
	Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 0.125, 2)) <>
	//~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 2, 3]) <>
	//~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 5]) <>
	~makeSubdivision.(
		Pseq([2, 1, 1, 1, 1], 1),
		Pseq(#[5, 5, 4], 1)
		//Pwrand([4, 5, 8], [1, 1, 0.25].normalizeSum, 1)
	)
	<> Pbind(\instrument, Pwrand([\grainSlicer, \fb1, \hh, \kick2], [1, 4, 1, 1].normalizeSum, inf))
);

)

Pdef(\player).stop;

(
Pdef(\player,
	Ppar([
		Pdef(\fb1Pattern),
		//Pdef(\kickPattern1),
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
//Ndef(\fx).stop;
//Ndef.clear;
//Ndef(\fx).ar(2);
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
Ndef(\fx).xset(\wet2, 0.3);
Ndef(\fx).filter(1, {|in| NHHall.ar(in, rt60: 0.1, stereo: 1, lowFreq: 200, lowRatio: 0.5, hiFreq: 4000, hiRatio: 0.5, earlyDiffusion: 0.5, lateDiffusion: 1, modRate: 0.2, modDepth: 0.2) });
//pitch shift
Ndef(\fx).set(\wet1, 2);
Ndef(\fx).filter(1, {|in| PitchShift.ar(in, 0.25, \pitch.kr(2), \pitchdispersion.kr(0.5), \timedispersion.kr(0.001))});
//spectral compander
Ndef(\fx).set(\wet3, 0.5);
Ndef(\fx).filter(3, { arg in, out=0, bufnum=0;
	var chain = FFT(~b.bufnum, in);
	chain = PV_Compander(chain, \companderThresh.kr(0) * 100, 2, 1.0);
	chain = PV_MagSmear(chain, MouseX.kr(0, 100));
	chain = PV_Diffuser(chain, MouseY.kr > 0.5 );
	Out.ar(0, Limiter.ar(IFFT(chain), 0.999, 0.05).dup);
});
//compressor
Ndef(\fx).filter(10, {|in| ~compressor.(in, 0.01, 0.05, -6, 4)});
//lfo mappings
Ndef(\fx).map(\timedispersion, Ndef(\lfo), \pitchdispersion, Ndef(\lfo3), \companderThresh, Ndef(\lfo2));
Ndef(\fx).map(\wet1, Ndef(\lfo4));
Ndef(\fx).play(fadeTime:0);
)