t.schedAbs(t.nextBar, {t.tempo_(165/60)});
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
	\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 40, 50) - 10,
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
	//~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3, 5, 7]) <>
	//~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 4]) <>
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
	//Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 2, 0.125)) <>
	~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 4, 5, 7]) <>
	~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 2, 4]) <>
	~makeSubdivision.(
		//Pseq(#[1, 1, 1, 1,], 1),
		//Pseq(#[2, 2, 2, 10], 1),
		Pseq(#[4, 3, 1, 2, 3, 0.75], 1),
		Pseq(#[2, 4, 4], 1)
	)
);

Pdef(\grainMod, Pbind(
	//\instrument, Prand([\hh, \kick2, \grainSlicer], inf),
	//\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 40, 50),
	\bufnum, ~src.bufnum,
	//\slice, ~pmodenv.(Pwhite(200, 150,inf), Pseq([1,1],inf)),
	\slice, Pkey(\cyclecount).stutter(Pwrand([1, 4, 2], [2, 1, 0.5].normalizeSum, inf)) + 100,
	\posRate, ~pmodenv.(Pkey(\groupdelta), Pkey(\dec)),
	\polarityMod, 1,
	\overlap, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 4, 20, -4), Pkey(\dec)),
	\panMod, Pwhite(0, Pkey(\bardelta), inf),
	\trigRate, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 200, 4000, -4), Pkey(\dec)).stutter(16),
	\pan_master, ~pmodenv.(Pseq([1, 0, -1],inf), Pkey(\dec)),
	//\gain, -16,
	\rate, 1
	)
);

Pdef(\grainSlicer_mono, Pmono(\grainSlicer_mono) <> Pbind(
	\dur, Pwhite(0.25, 1, inf) * 2,
	\dec, Pkey(\dur),
	\bufnum, ~src.bufnum,
	\slice, Pseries(0, 1, inf).wrap(0, 32).stutter(1) + 100,
	\posRate, ~pmodenv.(Pwhite(0.05, 0.5, inf), Pkey(\dec)),
	//\polarityMod, ~pmodenv.(Pwhite(0, 1, inf), Pkey(\dec), 1, \sine),
	\overlap, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 1, 40, -4), Pkey(\dec)),
	\panMod, Pwhite(1, 1, inf),
	\trigRate, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 200, 2000, 2, -8), Pkey(\dec)),
	\pan_master, ~pmodenv.(Pseq([-1, 0, 1],inf), Pkey(\dec), 1, \sine),
	\rate, 1,
	\gain, -12,
	)
);

Pdef(\fb1Mod,
	// PmonoArtic(\fb1_mono)<>
	Pbind(
	\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 40, 50),
	\dec, Pkey(\dur) * 1,
	//\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 2) * 1,
	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.125, 0).clip(0, 1).ampdb + dbamp(6),
//	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0, 0.5).clip(0, 1).ampdb,
	\sweep, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 2, 3, -4), Pkey(\dec)),
	\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf) * 0.5, Pseq([4,8,2,4],inf)),
	\time, ~pmodenv.(Pseq([0.005, 0.001, 0.010],inf) * 10 * Pkey(\groupdelta), Pseq([1, 4, 2],inf)),
	//\time, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 0.01, 0.05, -8), Pkey(\dec), 1, Pseq([\step, \sine, -3],inf)),
	//\res, ~pmodenv.(Pseq([1, 0],inf), Pseq([0.25, 0.25], inf)),
	//\res, 0,
	\damp, ~pmodenv.(Pseq([0.1, 1],inf), Pwhite(0.01, 0.4, inf)),
	//\damp, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 0.1, 0.5, -4), Pkey(\dec)),
	//\damp, 0.5,
	//\filter, 100,
	//\filter, ~pmodenv.(Pkey(\groupdelta).lincurve(0,1,2000, 500,-4), Pkey(\dec)),
	//\filter, ~pmodenv.(Pseq([60, 30],inf), Pseq([1, 1, 2], inf)),
	\filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),
	//\delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5,0.25], inf)),
	//\delay2, 0.5,
	//\exciter, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5,0.25], inf)),
	//\exciter, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 0, 0.5, -8), Pkey(\dur), 1, Pseq([\sine],inf)),
	//\exciter, 0.5,
	\impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pseq([1], inf)),
	\spont, ~pmodenv.(Pseq([1,30],inf), Pseq([4,4], inf)),
	\boost,  ~pmodenv.(Pseq([20000,200],inf), Pseq([1], inf)),
	\restore, ~pmodenv.(Pseq([1000,20000],inf), Pseq([0.5, 0.75],inf)),
	\dist,  ~pmodenv.(Pseq([16, 32],inf), Pseq([4, 4, 4],inf)),
	//\dist, 64,
	\rev, ~pmodenv.(Pexprand(0.1, 2, inf), Pseq([2, 4],inf)),
	\pan, ~pmodenv.(Pwhite(-0.8, 0.8, inf), Pseq([0.5, 0.25],inf)),
	//\gain, 0,
	)
);

Pdef(\percPattern1,
	PmonoArtic(\fb1)
	<> Pdef(\fb1Mod)
	<> Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 0.125, 2))
	//~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 2, 3]) <>
	<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 5])
	<> ~makeSubdivision.(
		Pseq([2, 1, 1, 1, 1], 1),
		Pseq(#[5, 5, 4], 1)
	)
);

Pdef(\percPattern2,
	Pdef(\grainMod)
	<> Pdef(\fb1Mod)
	<> Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 0.125, 2))
	//~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 2, 3]) <>
	<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 5])
	<> Pbind(\instrument, Pwrand([\fb1, \grainSlicer], [8, 4].normalizeSum, inf))
	<> ~makeSubdivision.(
		Pseq([2, 1, 1, 1, 1], 1),
		Pseq(#[5, 5, 4], 1)
	)
);
Pdef(\percPattern3,
	//PmonoArtic(\fb1) <>
//	Pfx(\echo, \dtime, 0.2, \decay, 3)
	Pdef(\grainMod)
	<> Pdef(\fb1Mod)
	<> Pdef(\kickMod)
	//Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 0.125, 2)) <>
	//~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 2, 3]) <>
	//~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 5]) <>
	<> Pbind(\instrument, Pwrand([\kick2, \fb1, \hh, \grainSlicer], [1, 8, 1, 2].normalizeSum, inf))
	<> ~makeSubdivision.(
		Pseq([2, 1, 1, 1, 1], 1),
		Pseq(#[5, 5, 4], 1)
	)
);

Pdef(\clapPattern,
	Pbind(
		\instrument, \clap,
		\dur, Pseq([Rest(2), 2], inf),
		\amp, 0.9,
	)
);

Pdef(\fmPad, Pmono(\fmPerc_mono) <> Pbind(
	\freq, 60,
	//\dur, Pwhite(0.25, 1, inf) * 0.5,
	\dec, Pkey(\dur),
	\carRatio, Pseq([1, 0.5, 2], inf).stutter(8),
	\m1Ratio, 7,
	\m2Ratio, 0.25,
	//\m2Ratio, ~pmodenv.(Prand([4, 8, 12, 2], inf), Pkey(\dur), 1, \exponential),
	\m3Ratio, 1.5,
	//\m3Ratio, ~pmodenv.(Prand([2, 1, 0.5], inf), Pkey(\dec), 1, \sine),
	/*
	\mod1fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
	\mod2fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
	\mod3fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
	\carfb, ~pmodenv.(Pwhite(0, 0.6), Pkey(\dec)),
	*/
	\index1, ~pmodenv.(Pexprand(0.25, 2, inf), Pkey(\dec)),
	\index2, ~pmodenv.(Pexprand(0.25, 2, inf), Pkey(\dec)),
	\iscale, ~pmodenv.(Pexprand(0.01, 0.005, inf), Pkey(\dec)),
	\feedback, ~pmodenv.(Pwhite(1, 0, inf), Pkey(\dec)),
	\fbmod, ~pmodenv.(Pwhite(0, 1, inf), Pkey(\dec)),
	\spread,~pmodenv.(Pwhite(0, 30, inf), Pkey(\dec)),
	\mix, ~pmodenv.(Pwhite(0.25, 0.75), Pkey(\dec)),
	//\mix, 0.5,
	//\drive, 0,
	\drive, ~pmodenv.(Pwhite(0, 6), Pkey(\dec)),
	\gain, -12
	)
);
)

Pdef(\player).play(t, quant:[1,0]);
Pdef(\player).stop;

(
Pdef(\player,
	Ppar([
		Pdef(\fmPad),
		])
	)
)


(
Pdef(\player,
	Ppar([
		Pdef(\percPattern2),
		Pdef(\fmPad),
		])
	)
)


(
Pdef(\player,
	Ppar([
		Pdef(\percPattern3),
		Pdef(\fmPad),
		])
	)
)

(
Pdef(\player,
	Ppar([
		Pdef(\percPattern3),
		Pdef(\fmPad),
		// Pdef(\fmPercPatt1)
		// Pdef(\grainSlicer_mono),
		Pdef(\kickPattern1),
		// Pdef(\hhPattern),
		// Pdef(\clapPattern),
		])
	)
)

(
Pdef(\player,
	Ppar([
		Pdef(\percPattern3),
		Pdef(\fmPad),
		// Pdef(\fmPercPatt1)
		// Pdef(\grainSlicer_mono),
		Pdef(\kickPattern1),
		// Pdef(\hhPattern),
		Pdef(\clapPattern),
		])
	)
)

(
Pdef(\player,
	Ppar([
		Pdef(\percPattern3),
		Pbindf(Pdef(\fmPad), \freq, 60),
		Pdef(\fmPercPatt1),
		// Pdef(\grainSlicer_mono),
		Pdef(\kickPattern1),
		// Pdef(\hhPattern),
		Pdef(\clapPattern),
		])
	)
)

(
Pdef(\player,
	Ppar([
		Pdef(\percPattern3),
		Pbindf(Pdef(\fmPad), \freq, 30),
		Pdef(\fmPercPatt1),
		// Pdef(\grainSlicer_mono),
		Pdef(\kickPattern1),
		// Pdef(\hhPattern),
		Pdef(\clapPattern),
		])
	)
)

(
Pdef(\player,
	Ppar([
		Pdef(\percPattern3),
		Pbindf(Pdef(\fmPad), \freq, 30),
		Pdef(\fmPercPatt1),
		// Pdef(\grainSlicer_mono),
		Pdef(\kickPattern1),
		Pdef(\hhPattern),
		Pdef(\clapPattern),
		])
	)
)

(
Pdef(\player,
	Ppar([
		// Pdef(\percPattern3),
		Pbindf(Pdef(\fmPad), \freq, 30),
		Pdef(\fmPercPatt2),
		// Pdef(\grainSlicer_mono),
		Pdef(\kickPattern1),
		Pdef(\hhPattern),
		Pdef(\clapPattern),
		])
	)
)

(
Pdef(\player,
	Ppar([
		Pdef(\percPattern3),
		// Pbindf(Pdef(\fmPad), \freq, 80),
		Pdef(\fmPercPatt4),
		// Pdef(\grainSlicer_mono),
		Pdef(\kickPattern1),
		// Pdef(\hhPattern),
		Pdef(\clapPattern),
		])
	)
)

s.queryAllNodes

Pdef(\player).play(t, quant:[1,0]);
