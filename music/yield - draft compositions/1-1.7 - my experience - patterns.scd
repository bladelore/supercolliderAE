t.schedAbs(t.nextBar, {t.tempo_(165/60)});
Pdef.clear;

FluidWaveform(~src, ~indices);

(
var file = "/Users/aelazary/Desktop/Samples etc./Film Sounds/102060__juskiddink__finnish-sauna.wav";
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

~featuresSorted

(
~mainout = 0;
~longverb = Bus.audio(s,2);
~longverb2 = Bus.audio(s,2);
~shortverb = Bus.audio(s,2);
~delay = Bus.audio(s,2);
~chorus = Bus.audio(s,2);

~modDelay=Bus.audio(s,2);
~mod1_in = Bus.audio(s, 2);
~mod1_out = Bus.control(s, 1);

~convolve_A=Bus.audio(s,2);
~convolve_B=Bus.audio(s,2);
~comp= Bus.audio(s,2);
)

(

Pdef(\hhMod, Pbind(
	\amp, 1,
	\legato, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.25, 0),
	\atk, ~nearestDelta.(~pattern, Pkey(\groupdelta),  0.5, 0.75) * 0.125,
	\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 30, 80),
	\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.25, 0.5).clip(0, 1).ampdb.min(-12),
	\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.33, 0) * 0.125,
	// \pan, Pkey(\groupdelta).linlin(0, 1, -1, 1),
	\gain, -16,
	)
);

Pdef(\hhPattern1,
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

Pdef(\kickMod, Pbind(
	\amp, 1,
	\legato, ~nearestDelta.(~pattern, Pkey(\bardelta), 0.25, 0).max(0.5),
	//\atk, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.125, 1).linexp(0, 1, 0.1, 0.3),
	\freq, ~nearestDelta.(~pattern, Pkey(\bardelta),  0.75, -1).linexp(0, 1, 40, 50) - 10,
	//\gain, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.125, 0).clip(0, 1).ampdb.min(-12) + dbamp(6),
	\dec, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.2, 1).linexp(0, 1, 1, 0.2).clip(0.01, 2),
	\sweep, ~nearestDelta.(~pattern, Pkey(\groupdelta), 0.1, 3).linexp(0, 1, 8, 0.1),
	\pan, Pkey(\groupdelta).linlin(0, 1, -0.3, 0.3),
	\gain, -6
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
	\amp, 1,
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

Pdef(\grainPattern, Pmono(\grainSlicer_mono) <> Pbind(
	\dur, Pwhite(0.25, 1, inf) * 2,
	\dec, Pkey(\dur) * 1,
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

Pdef(\segPattern, Pbind(
	\amp, 1,
	\bufnum, ~src.bufnum,
	\dec, Pkey(\dur) * 1,
	\sliceStart, 68,
	\slice, Pseries(0, 1, inf).wrap(0, 32).stutter(4) + Pkey(\sliceStart),
	\pan, ~pmodenv.(Pseq([-0.3, 0 ,0.3],inf), Pkey(\dec), 1, \linear),
	//\pan, 0,
	\atk, 0.001,
	\rel, 0.4,
	// \rel, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 1, 0.4, -4), Pkey(\dec), 1),
	\curve, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, -8, -20), Pkey(\dec), 1),
	\rate, 1,
	// \gain, 12,
	\gain, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 9, 3, -4), Pkey(\dec), 1),
	//pitchshifter
	\pitchMix, 0.7,
	\pitchRatio, 2,
	\windowSize, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.01, 0.25), Pkey(\dec), 1, \sine),
	\pitchDispersion, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 1, 0.01), Pkey(\dec), 1, \sine),
	\timeDispersion, 0.01,
	)
	<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 4])
	<> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3])
// <> Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 1, 1.25))
	<> ~makeSubdivision.(
		Pseq([2, 2, 1, 1], 1),
		Pseq(#[2, 2, 4, 2, 2, 4], 1)
	)
	<> Pbind(\instrument, \segPlayer)
);

Pdef(\segPattern2, Pbind(
	\amp, 1,
	\bufnum, ~src.bufnum,
	\dec, Pkey(\dur) * 1,
	\sliceStart, 40,
	\slice, Pseries(0, 1, inf).wrap(0, 32).stutter(2) + Pkey(\sliceStart),
	\pan, ~pmodenv.(Pseq([-0.3, 0 ,0.3],inf), Pkey(\dec), 1, \linear),
	//\pan, 0,
	\atk, 0.001,
	\rel, 0.8,
	// \rel, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 1, 0.4, -4), Pkey(\dec), 1),
	\curve, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, -8, -20), Pkey(\dec), 1),
	\rate, 2,
	// \gain, 12,
	\gain, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 9, 3, -4), Pkey(\dec), 1),
	//pitchshifter
	\pitchMix, 0.7,
	\pitchRatio, 2,
	\windowSize, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.01, 0.25), Pkey(\dec), 1, \sine),
	\pitchDispersion, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 1, 0.01), Pkey(\dec), 1, \sine),
	\timeDispersion, 0.01,
	)
<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 4])
// <> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3])
// <> Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 1, 1.25))
	<> ~makeSubdivision.(
		Pseq([1.5, 1.5, 1, 1, 2, 1], 1),
		Pseq(#[2, 2, 4, 2, 2, 4], 1)
	)
	<> Pbind(\instrument, \segPlayer)
);

Pdef(\fb1Mod,
	// PmonoArtic(\fb1_mono)<>
	Pbind(
	\amp, 1,
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
		\delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5,0.25], inf)),
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
Pdef(\percPattern2,
	Pdef(\grainMod)
	<> Pdef(\fb1Mod)
	<> Pdef(\kickMod)
	//Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 0.125, 2)) <>
	//~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 2, 3]) <>
	//~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 5]) <>
	<> Pbind(\instrument, Pwrand([\kick2, \fb1, \hh, \grainSlicer], [1, 8, 1, 2].normalizeSum, inf))
	<> ~makeSubdivision.(
		Pseq([2, 1, 1, 1, 1], 1),
		// Pseq(#[5, 5, 4], 1)
		Pseq(#[4, 4, 4], 1)
	)
);

Pdef(\percPattern3,
	Pdef(\grainMod)
	<> Pdef(\fb1Mod)
	<> Pdef(\kickMod)
	//Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 0.125, 2)) <>
	//~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 2, 3]) <>
	//~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 5]) <>
	<> Pbind(\instrument, Pwrand([\kick2, \fb1, \hh, \grainSlicer], [4, 8, 1, 2].normalizeSum, inf))
	<> ~makeSubdivision.(
		Pseq([2, 1, 1, 1, 1], 1),
		Pseq(#[5, 5, 4], 1)
	)
);

Pdef(\percPattern4,
	Pdef(\grainMod)
	<> Pdef(\fb1Mod)
	<> Pdef(\kickMod)
	//Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 0.125, 2)) <>
	//~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 2, 3]) <>
	//~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 5]) <>
	<> Pbind(\instrument, Pwrand([\kick2, \fb1, \hh, \grainSlicer], [1, 8, 1, 2].normalizeSum, inf))
	<> ~makeSubdivision.(
		Pseq([2, 1, 1, 1, 1], 1),
		Pseq(#[4, 5, 4], 1)
	)
);

Pdef(\percPattern5,
	Pdef(\grainMod)
	<> Pdef(\fb1Mod)
	<> Pdef(\kickMod)
	//Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 0.125, 2)) <>
	//~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 2, 3]) <>
	//~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 5]) <>
	<> Pbind(\instrument, Pwrand([\kick2, \fb1, \grainSlicer], [1, 8, 2].normalizeSum, inf))
	<> ~makeSubdivision.(
		Pseq([2, 1, 1, 1, 1], 1),
		Pseq(#[5, 5, 4], 1)
	)
);

Pdef(\clapPattern,
	Pbind(
		\instrument, \clap,
		\dur, Pseq([Rest(2), 2], inf),
		\amp, 0.8,
		\out, [~mainout, ~longverb]
	)
);

Pdef(\clapPattern2,
	Pbind(
		\instrument, \clap,
		\dur, Pseq([Rest(2), 2], inf) * 2,
		\amp, 0.8,
		\out, [~mainout, ~longverb]
	)
);

Pdef(\subPattern,
	PmonoArtic(
		\simpleSub,
		\amp, 1,
		\legato, 0.9,
		\dur, Pseq(
			[
				1, Rest(15), Rest(1), 1, Rest(14),
				1, Rest(15), Rest(0.5), 1, Rest(14),
			],inf),
		\gain, -6,
		\freq, 40
	)
);

Pdef(\subPattern2,
	PmonoArtic(
		\simpleSub,
		\amp, 1,
		// \legato, 0.9,
		\dur, Pseq(
			[
				1, Rest(15), Rest(1), 1, Rest(14),
				1, Rest(15), Rest(0.5), 1.5, Rest(14),
				1, 1, Rest(14), Rest(1), 1, Rest(14),
				Rest(0.5), 1, 1.5, Rest(13), 1, Rest(14),
			],inf),
		\gain, -6,
		\freq, 40
	)
);

Pdef(\subPattern3,
	PmonoArtic(
		\simpleSub,
		\amp, 1,
		\legato, 0.9,
		\dur, Pseq([
			1, 1, Rest(14), Rest(1.5), 1.5, Rest(13),
			1.5, 1.5, 3, Rest(10), Rest(1.5), 1.5, Rest(13)
		], inf),
		\gain, -6,
		\freq, Pseq([28, 28, 29].midicps, inf),
	)
);

Pdef(\fmPad, Pmono(\fmPerc_mono) <> Pbind(
	\freq, 30,
	\amp, 1,
	//\dur, Pwhite(0.25, 1, inf) * 0.5,
	\dec, Pkey(\dur),
	\carRatio, Pseq([1, 0.5, 2], inf).stutter(8),
	\m1Ratio, 7,
	\m2Ratio, 0.25,
	\m3Ratio, 1.5,
	\index1, ~pmodenv.(Pexprand(0.25, 2, inf), Pkey(\dec)),
	\index2, ~pmodenv.(Pexprand(0.25, 2, inf), Pkey(\dec)),
	\iscale, ~pmodenv.(Pexprand(0.01, 0.005, inf), Pkey(\dec)),
	\feedback, ~pmodenv.(Pwhite(1, 0, inf), Pkey(\dec)),
	\fbmod, ~pmodenv.(Pwhite(0, 1, inf), Pkey(\dec)),
	\spread,~pmodenv.(Pwhite(0, 30, inf), Pkey(\dec)),
	\mix, ~pmodenv.(Pwhite(0.25, 0.75), Pkey(\dec)),
	\drive, ~pmodenv.(Pwhite(0, 6), Pkey(\dec)),
	\gain, -12,
	\out, [~mainout, ~shortverb]
	)
);

Pdef(\fmPad4,
		Pbindf(
			Pdef(\fmPad),
			\freq, 15,
			// \carRatio, Pseq([1, 0.5, 2], inf).stutter(8),
			\carRatio, 0.5,
			\m1Ratio, 7,
			\m2Ratio, 26,
			\m3Ratio, 1.5,
			\mix, 1,
			\gain, 30,
		)
);

Pdef(\fmPad2,
		Pbindf(
			Pdef(\fmPad),
			// \carRatio, Pseq([1, 0.5, 2], inf).stutter(8),
			\freq, 15,
			\carRatio, 0.5,
			\m1Ratio, 8,
			\m2Ratio, 25,
			\m3Ratio, 2,
			\mix, 1,
			\gain, 30,
		)
);

Pdef(\fmPad3,
		Pbindf(
			Pdef(\fmPad),
			// \carRatio, Pseq([1, 0.5, 2], inf).stutter(8),
			\carRatio, 0.5,
			\m1Ratio, 7,
			\m2Ratio, 13,
			\m3Ratio, 0.75,
			\mix, 1,
			\gain, 10,
		)
);



Pdef(\fmPad5,
		Pbindf(
			Pdef(\fmPad),
			\freq, 15,
			// \carRatio, Pseq([1, 0.5, 2], inf).stutter(8),
			\carRatio, 2,
			\m1Ratio, 8,
			\m2Ratio, 26,
			\m3Ratio, 2.01,
			\mix, 1,
			\gain, 30,
		)
);

Pdef(\fmPercPatt1,
	// Pmono(\fmPerc_mono) <>
	Pbind(
		\amp, 1,
		\freq, 35.midicps,
		//\dur, 0.5,
		\dec, Pkey(\dur) * 0.75,
		//\atk,0,
		// \atk, ~pmodenv.(Pkey(\groupdelta), Pkey(\dec)),
		// \rel, ~pmodenv.(Pwhite(1, 0.25, inf), Pkey(\dec)),
		\rel, Pkey(\dec),
		\feedback, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
		\fbmod, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
		\carRatio, 0.5,
		\m1Ratio, 1,
		\m2Ratio, 7,
		\m3Ratio, 9,
		\index1, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
		\index2, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
		\iscale, ~pmodenv.((1 - Pkey(\cycledelta)) + 1e-4, Pkey(\dec)),
		\spread, ~pmodenv.(Pwhite(10, 30, inf), Pkey(\dec)),
		\mix, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		//\mix, 0,
		// \drive, 16,
		\drive, ~pmodenv.(Pkey(\groupdelta) * 16, Pkey(\dec)),
		\gain, -16,
	)
	// <> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3])
	<> ~makeSubdivision.(
		Pseq([1.5, 1.5, 1, 2], 1),
		Pseq([2, 2, 4], 1)
	)
	<> Pbind(
		\instrument, \fmPerc,
	)
);

Pdef(\fmPercPatt2,
	// Pmono(\fmPerc_mono) <>
	Pbind(
		\amp, 1,
		\freq, 60,
		//\dur, 0.5,
		// \dur, Pwhite(0.5, 0.125, inf) * 2,
		\dec, Pkey(\dur) * 0.5,
		//\atk,0,
		// \atk, ~pmodenv.(Pkey(\groupdelta) * 0.5, Pkey(\dec)),
		// \rel, ~pmodenv.(Pwhite(0.5, 1, inf), Pkey(\dec)),
		//\rel, 0.5,
		\rel, Pkey(\dec),
		\feedback, ~pmodenv.(Pwhite(0, 1, inf), Pkey(\dec)),
		\fbmod, ~pmodenv.(Pwhite(0, 1, inf), Pkey(\dec)),

		\carRatio, 0.5,
		\m1Ratio, 2,
		\m2Ratio, 3,
		\m3Ratio, 5,

		\index1, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
		\index2, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
		\iscale, ~pmodenv.(Pkey(\cycledelta) + 1e-4, Pkey(\dec)),
		// \spread,~pmodenv.(Pwhite(10, 30, inf), Pkey(\dec)),
		\mix, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		//\mix, 0,
		// \drive, 16,
		\drive, ~pmodenv.(Pkey(\groupdelta) * 16, Pkey(\dec)),
		\gain, -16,
	)
	// <> Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 2, 0.125))
	// <> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3])
	<> ~makeSubdivision.(
		Pseq([1.5, 1.5, 1, 1], 1),
		Pseq(#[1, 2, 4], 1)
	)
	<> Pbind(\instrument, \fmPerc)
);

Pdef(\fmPercPatt3,
	// Pmono(\fmPerc_mono) <>
	Pbind(
		\amp, 1,
		\instrument, \fmPerc,
		// \freq, Pwrand([23, 24].midicps, [0.8, 0.2], inf),
		\freq, 23.midicps,
		// \freq, 30,
		\dur, Pwhite(0.5, 0.125, inf) * 2,
		//\dur, 0.5,
		\dec, Pkey(\dur) * 1,
		\atk, ~pmodenv.(Pwhite(0.01, 0.5, inf), Pkey(\dec)),
		// \rel, ~pmodenv.(Pwhite(0.5, 0.125, inf), Pkey(\dec)),
		\rel, 0.2,
		\feedback, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
		\fbmod, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
		\carRatio, 1,
		\m1Ratio, 1,
		\m2Ratio, 7,
		\m3Ratio, 9,
		\index1, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
		\index2, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
		\iscale, ~pmodenv.(Pexprand(1, 0.1, inf), Pkey(\dec)),
		\spread,~pmodenv.(Pwhite(10, 30, inf), Pkey(\dec)),
		\mix, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		// \mix, 0,
		// \drive, 16,
		\drive, ~pmodenv.(Pwhite(0, 16), Pkey(\dec)),
		\gain, -24,
	)
);

Pdef(\fmPercPatt4,
	// Pmono(\fmPerc_mono) <>
	Pbind(
		\amp, 1,
		\instrument, \fmPerc,
		\freq, 30,
		\dur, Pwhite(0.5, 0.125, inf),
		//\dur, 0.5,
		\dec, Pkey(\dur) * 1,
		\atk, ~pmodenv.(Pwhite(0.01, 0.5, inf), Pkey(\dec)),
		//\rel, ~pmodenv.(Pwhite(0.5, 0.125, inf), Pkey(\dec)),
		\rel, 0.2,
		\feedback, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
		\fbmod, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
		\carRatio, 0.5,
		\m1Ratio, 1,
		\m2Ratio, 7,
		\m3Ratio, 9,

		\i1atk, ~pmodenv.(Pwhite(0.5, 0.1), Pkey(\dec)),
		\i2atk, ~pmodenv.(Pwhite(0.8, 0.01), Pkey(\dec)),
		\i1rel, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\i2rel, ~pmodenv.(Pwhite(0, 0.5), Pkey(\dec)),

		\index1, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
		\index2, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
		\iscale, ~pmodenv.(Pexprand(1, 0.1, inf), Pkey(\dec)),
		\spread,~pmodenv.(Pwhite(10, 30, inf), Pkey(\dec)),
		\mix, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		//\mix, 0,
		//\drive, 0,
		\drive, ~pmodenv.(Pwhite(0, 16), Pkey(\dec)),
		\gain, -28,
	)
);

Pdef(\follower1,
	Pmono(\envfollow,
		\amp, 1,
		\dur, 0.1,
		\atk, 0.01,
		\rel, 0.1,
		\amp, 1,
		\mul, 0.001,
		\add, 0.01,
		\inbus, ~mod1_in,
		\out, ~mod1_out,
	)
);

Pdef(\modDelay,
		Pmono(\combDelay,
		\amp, 1,
			\inbus, ~modDelay,
			\addAction, \addToTail,
		// \fb, ,
			\fb, 0.9,
			\dur, 0.1,
			\size, ~mod1_out.asMap,
			\hpf, 600,
			\lpf, 3000,
			\sep, ~mod1_out.asMap * 0.1,
			\gain, -12,
			\callback, { Pdefn(\fxid, ~id) },
		)
);

Pdef(\longverb,
	Pmono(\greyhole,
		\amp, 1,
		// \dur, 0.01,
		\inbus, ~longverb,
		\addAction, \addToTail,
		\dtime, 0.5,
		\damp, 0.9,
		\size, 10,
		\diff,  0.707,
		\fb, 0.1,
		\modDepth, 0,
		\modFreq, 2,
		\gain, -20,
		\mix, 1,
		\callback, { Pdefn(\fxid, ~id) },
	)
);

Pdef(\longverb2,
	Pmono(\nhverb,
		\amp, 1,
		// \dur, 0.01,
		\inbus, ~longverb2,
		\addAction, \addToTail,
		\rt60, 10,
		\lowFreq, 400,
		\mix, 1,
		\gain, 0,
		\callback, { Pdefn(\fxid, ~id) },
	)
);

Pdef(\shortverb,
	Pmono(\nhverb,
		\amp, 1,
		\inbus, ~shortverb,
		\addAction, \addToTail,
		\lowFreq, 500,
		// \damping, 0.9,
		// \gain, -12,
		\earlyDiffusion, 0.3,
		\lateDiffusion, 0.1,
		\rt60, 2,
		\gain, -12,
		\callback, { Pdefn(\fxid, ~id) },
	)
);

Pdef(\delay,
	Pmono(\echo,
		\amp, 1,
		\inbus, ~delay,
		\addAction, \addToTail,
		\fb, 0.5,
		\dur, 4,
		\size, Pseq([1/4, 3/8], inf),
		// \size, ~pmodenv.(Pwhite(0.01, 0.02), 0.5),
		\hpf, 30,
		\lpf, 8000,
		\sep, ~pmodenv.(Pwhite(0.01, 1), 0.5),
		\gain, -16,
		\callback, { Pdefn(\fxid, ~id) },
	)
);

Pdef(\chorus,
	Pmono(\choruscompresseffect,
		\amp, 1,
	\inbus, ~chorus,
	\addAction, \addToTail,
	\gain, -16,
	\callback, { Pdefn(\fxid, ~id) },
	)
);

Pdef(\convolve,
	Pmono(\convolve_AB,
		\amp, 1,
	\inbus_A, ~convolve_A,
	\inbus_B, ~convolve_B,
	\addAction, \addToTail,
	// \gain, -16,
	\callback, { Pdefn(\fxid, ~id) },
	)
);

Pdef(\comp,
	Pmono(\comp,
		\amp, 1,
		\inbus, ~comp,
		\addAction, \addToTail,
		\ratio, 6,
		\thresh, -40,
		\atk, 0.1,
		\rel, 100,
		\auto, 1,
		\callback, { Pdefn(\fxid, ~id) },
	)
);

)

