Pdef.clear;

(
a = Dictionary();
~analyzeSlices.("C:/Users/Asher/Desktop/Samples etc/Field recs/Brush metal bowl.wav", a, 0.2, \centroid);

b = Dictionary();
~analyzeSlices.("C:/Users/Asher/Desktop/Samples etc/Field recs/Big bowl flick.wav", b, 0.2, \crest);

c = Dictionary();
~analyzeSlices.("C:/Users/Asher/Desktop/Samples etc/Field recs/Brush metal bowl small.wav", c, 0.2, \crest);

d = Dictionary();
~analyzeSlices.("C:/Users/Asher/Desktop/Samples etc/Silent Hill/Drum Loops/Silent Hill Origins/Snowblind/05 drums02.wav", d, 0.2, \flatness);

e = Dictionary();
~analyzeSlices.("C:/Users/Asher/Desktop/Samples etc/hollywood edge - foley sound library/FSL-04/FSL-04-Stab, Cut, And Guts; Stab And Twist With Grind And Thick Blood Gush. - Guts Gush.wav", e, 0.1, \crest);
)

(
~mainout = 0;
~longverb = Bus.audio(s,2);
~nhverb = Bus.audio(s,2);
~miVerb = Bus.audio(s,2);
~delay = Bus.audio(s,2);
~chorus = Bus.audio(s,2);

~modDelay=Bus.audio(s,2);
~mod1_in = Bus.audio(s, 2);
~mod1_out = Bus.control(s, 1);

~modal=Bus.audio(s,2);
~formant=Bus.audio(s,2);
~grain=Bus.audio(s,2);
~tape=Bus.audio(s,2);
~pitchShift=Bus.audio(s,2);
~filter=Bus.audio(s,2);
~eq=Bus.audio(s,2);

~bufA = Buffer.alloc(s, s.sampleRate * 0.1);
~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
~bufC = Buffer.alloc(s, s.sampleRate * 0.025);

~convolve_A=Bus.audio(s,2);
~convolve_B=Bus.audio(s,2);

~morph_A=Bus.audio(s,2);
~morph_B=Bus.audio(s,2);

//fx routing
Pdef(\modal, Pmono(\padKlank, \inbus, ~modal, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\formant, Pmono(\formantBank, \inbus, ~formant, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\grain, Pmono(\liveGrain_mono, \inbus, ~grain, \bufnum, ~bufA, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\tape, Pmono(\tape, \inbus, ~tape, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\pitchShift, Pmono(\pitchShift, \inbus, ~pitchShift, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\filter, Pmono(\vasem12, \inbus, ~filter, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\eq, Pmono(\EQstack, \inbus, ~eq, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));

Pdef(\morph,
	Pmono(\cepstralMorph_fx,
		\amp, 1,
		\inbus_A, ~convolve_A,
		\inbus_B, ~convolve_B,
		\addAction, \addToTail,
		\callback, { Pdefn(\fxid, ~id) },
	)
);

Pdef(\nhverb,
	Pmono(\nhverb,
		\amp, 1,
		// \dur, 0.01,
		\inbus, ~nhverb,
		\addAction, \addToTail,
		\gain, 0,
		\callback, { Pdefn(\fxid, ~id) },
	)
);

Pdef(\miVerb,
	Pmono(\miVerb,
		\amp, 1,
		// \dur, 0.01,
		\inbus, ~miVerb,
		\addAction, \addToTail,
		\gain, 0,
		\callback, { Pdefn(\fxid, ~id) },
	)
);

)

(
Pdef(\modalPatt,
	Pbind(
		\amp, 1,
		\dec, Pkey(\dur) * 1,
		\sustainTime, Pkey(\dur) * 0.01,
		\atk, 0.05,
		\rel, Pkey(\dur) * 5,
		\pan, Pwhite(-0.5, 0.5),
		\f0, 90,
		\structure, 2.5,
		\brightness, Pkey(\groupdelta).linlin(0, 1, 0.5, 0.75),
		\damping, Pkey(\groupdelta),
		\accent, 0.5,
		\harmonicstretch, 0.75,
		\position, Pkey(\cycledelta),
		\loss, Pwhite(0.1, 1),
		// \modeNum, 3,
		\cosFreq, ~pmodenv.(Pwhite(0.1, 1), Pkey(\dur)),
	)
	<> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3, 4])
	// <> Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 2, 1))
	// <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
	// <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\cycledelta))
	// <> Pbind(\timingOffset, Pkey(\cyclecount) / Pkey(\groupdelta))
	<> ~makeSubdivision.(
		Pseq([1.5, 1, 1, 1, 0.5], 1),
		Pseq([4, 4, 5], 1)
	)
	<> Pbind(\instrument, \rongsinator)
);

Pdef(\seq2,
	Pbind(
		\amp, 1,
		\dec, Pkey(\dur) * 1,
		\sustainTime, Pkey(\dur) * 0.01,
		\atk, 0.05,
		\rel, Pkey(\dur) * 2,
		\pan, Pwhite(-0.5, 0.5),
		\f0, 180,
		\structure, 0.75,
		\brightness, Pkey(\groupdelta).linlin(0, 1, 0.5, 0.75),
		\damping, Pkey(\groupdelta),
		\accent, 0.5,
		\harmonicstretch, 0.9,
		\position, Pkey(\cycledelta),
		\loss, Pwhite(0.1, 1),
		\cosFreq, Pwhite(0.1, 1),
	)
	<> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3, 4])
	// <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
	// <> Pbind(\timingOffset, Pkey(\cyclecount) / Pkey(\groupdelta))
	<> ~makeSubdivision.(
		Pseq([1.5, 1, 1, 1, 0.5], 1),
		Pseq([4, 4, 5], 1)
	)
	<> Pbind(\instrument, \rongsinator)
);

Pdef(\kickDrone,
	Pbind(
		\amp, 1,
		\freq, 50,
		\atk, 1,
		\dec, 2,
		\fb, 1,
		\ratio, 4,
		\index, 0.25,
		\gain, -26,
	)
	<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3])
	<> ~makeSubdivision.(
		Pseq([1.5, 1.5, 1.5, 1.5, 1.5], 1),
		Pseq([3, 3, 4], 1)
	)
	<> Pbind(\instrument, \fmKick)
);

Pdef(\segPattern, Pbind(
	\amp, 1,
	\buf, a.at(\file),
	\dec, Pkey(\dur) * 1,
	\sliceStart, 60,
	\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(3) + Pkey(\sliceStart), a),
	\pan, ~pmodenv.(Pseq([-0.3, 0 ,0.3],inf), Pkey(\dec), 1, \linear),
	//\pan, 0,
	\atk, 0.01,
	\rel, 4,
	// \rel, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 1, 0.4, 0), Pkey(\dec), 1),
	\curve, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, -8, 0), Pkey(\dec), 1),
	\rate, 1,
	// \gain, 12,
	\gain, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 9, 3, -4), Pkey(\dec), 1),
	//pitchshifter
	\pitchMix, 0,
	\pitchRatio, 0.5,
	\windowSize, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.01, 0.25), Pkey(\dec), 1, \sine),
	\pitchDispersion, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.1, 0.01), Pkey(\dec), 1, \linear),
	\timeDispersion, 0.01,
)
<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[2, 4])
<> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3])
<> ~makeSubdivision.(
	Pseq([1.5, 1, 1, 1, 0.5], 1),
	Pseq(#[2, 2, 4, 2, 2, 4], 1)
)
<> Pbind(\instrument, \segPlayer)
);

Pdef(\drumSliced,
	Pbind(
		\amp, 1,
		\buf, d.at(\file),
		//lowest to highest
		\overlap, 100,
		// \trigRate, 100,
		\trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
		\slice, ~pGetSlice.(Pseries(3, 1, inf).wrap(0, 32).stutter(3), d),
		// \gain, 6,
		\posRate, 5
		// \rate, 2,
	)
	// <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
	<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3])
	<> ~makeSubdivision.(
		Pseq([2, 4, 6, 4, 2], 1),
		// Pseq([1, 1, 1, 4, 2], 1),
		Pseq([4, 4, 6], 1)

	) <> Pbind(\instrument, \grainSlicer)
);

Pdef(\stab, Pbind(
	\amp, 1,
	\buf, e.at(\file),
	\dec, Pkey(\dur) * 1,
	\sliceStart, 5,
	\sliceStutter, 3,
	\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), e),
	//\pan, 0,
	\atk, 0.05,
	// \rel, 1.5,
	\rel, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 1, 2, 0.75), Pkey(\dec), 1),
	// \curve, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, -8, 0), Pkey(\dec), 1),
	\rate, 2,
	// \gain, 12,
	// \gain, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 9, 3, -4), Pkey(\dec), 1),
	//pitchshifter
	\pitchMix, 0.4,
	\pitchRatio, 0.5,
	\windowSize, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.01, 0.25), Pkey(\dec), 1, \sine),
	\pitchDispersion, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.1, 0.01), Pkey(\dec), 1, \linear),
	\timeDispersion, 0.01,
)
// <> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[2, 4])
<> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1])
// <> Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 1, 1.125))
<> ~makeSubdivision.(
	Pseq([1.5, 1, 1, 1, 0.5], 1),
	Pseq(#[2, 2, 4, 2, 2, 4], 1)
)
<> Pbind(\instrument, \segPlayer)
);

Pdef(\stab2, Pbind(
	\amp, 1,
	\buf, e.at(\file),
	\dec, Pkey(\dur) * 1,
	\sliceStart, 5,
	\sliceStutter, 2,
	\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), e),
	//\pan, 0,
	\atk, 0.05,
	// \rel, 1.5,
	\rel, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 0.5, 2, 0.75), Pkey(\dec), 1),
	\curve, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, -8, 0), Pkey(\dec), 1),
	\rate, 1,
	// \gain, 12,
	// \gain, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 9, 3, -4), Pkey(\dec), 1),
	//pitchshifter
	\pitchMix, 0.4,
	\pitchRatio, 0.5,
	\windowSize, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.01, 0.25), Pkey(\dec), 1, \sine),
	\pitchDispersion, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.1, 0.01), Pkey(\dec), 1, \linear),
	\timeDispersion, 0.01,
)
<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[2, 4])
// <> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[2, 4])
// <> Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 1, 1.125))
<> ~makeSubdivision.(
	Pseq([1.5, 1, 1, 1, 0.5], 1),
	Pseq(#[2, 2, 4, 2, 2, 4], 1)
)
<> Pbind(\instrument, \segPlayer)
);

Pdef(\stab3, Pbind(
	\amp, 1,
	\buf, e.at(\file),
	\dec, Pkey(\dur) * 1,
	\sliceStart, 5,
	\sliceStutter, 1,
	\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), e),
	//\pan, 0,
	\atk, 0.01,
	// \rel, 1.5,
	// \rel, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 0.5, 2, 0.75), Pkey(\dec), 1),
	\curve, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, -8, 0), Pkey(\dec), 1),
	\rate, 1,
	// \gain, 12,
	// \gain, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 9, 3, -4), Pkey(\dec), 1),
	//pitchshifter
	\pitchMix, 0,
	\pitchRatio, 2,
	\windowSize, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.01, 0.25), Pkey(\dec), 1, \sine),
	\pitchDispersion, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.1, 0.01), Pkey(\dec), 1, \linear),
	\timeDispersion, 0.01,
)
// <> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 4])
<> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 4, 6])
// <> Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 1, 1.125))
<> ~makeSubdivision.(
	Pseq([1.5, 1, 1, 1, 0.5] * 2, 1),
	Pseq(#[2, 2, 4, 2, 2, 4], 1)
)
<> Pbind(\instrument, \segPlayer)
);

Pdef(\stab4, Pbind(
	\amp, 1,
	\buf, e.at(\file),
	\dec, Pkey(\dur) * 1,
	\sliceStart, 5,
	\sliceStutter, 1,
	\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), e),
	//\pan, 0,
	\atk, 0.01,
	// \rel, 1.5,
	// \rel, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 0.5, 2, 0.75), Pkey(\dec), 1),
	\curve, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, -8, 0), Pkey(\dec), 1),
	\rate, 1,
	// \gain, 12,
	// \gain, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 9, 3, -4), Pkey(\dec), 1),
	//pitchshifter
	\pitchMix, 0,
	\pitchRatio, 2,
	\windowSize, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.01, 0.25), Pkey(\dec), 1, \sine),
	\pitchDispersion, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.1, 0.01), Pkey(\dec), 1, \linear),
	\timeDispersion, 0.01,
)
// <> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 4])
// <> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 4, 6])
// <> Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 1, 1.125))
<> ~makeSubdivision.(
	Pseq([1.5, 1, 1, 1, 0.5] * 2, 1),
	Pseq(#[2, 2, 4, 2, 2, 4], 1)
)
<> Pbind(\instrument, \segPlayer)
);

Pdef(\kick0, Pbind(
	\amp, 1,
	\freq, 40,
	\decay, 10,
	\tone, 0.2,
	\attackfm, 0.3,
	// \selffm, 0.25
	\selffm, 0.11
)
<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[2])
<> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3])
<> ~makeSubdivision.(
	Pseq([1.5, 1, 1, 1, 0.5], 1),
	Pseq(#[2, 2, 4, 2, 2, 4], 1)
)
<> Pbind(\instrument, \AnalogBassDrum)
);

Pdef(\kick, Pbind(
	\amp, 1,
	\freq, 40,
	\decay, 10,
	\tone, 0.2,
	\attackfm, 0.3,
	// \selffm, 0.25
	\selffm, 0.11
)
<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[2, 4])
<> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[2, 4])
<> ~makeSubdivision.(
	Pseq([1.5, 1, 1, 1, 0.5], 1),
	Pseq(#[2, 2, 4, 2, 2, 4], 1)
)
<> Pbind(\instrument, \AnalogBassDrum)
);

Pdef(\kick2, Pbind(
	\amp, 1,
	\freq, 40,
	\dec, Pkey(\cycledelta).linlin(0, 1, 0.5, 0.1),
	\sweep, 1,
	\drive, -10,
	\gain, -3,
)
<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 4])
// <> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3])
<> ~makeSubdivision.(
	Pseq([1, 2, 1] * 2, 1),
	Pseq(#[4, 2, 1, 2, 2], 1)
)
<> Pbind(\instrument, \kick2)
);

Pdef(\kick3, Pbind(
	\amp, 1,
	\freq, 40,
	\dec, Pkey(\cycledelta).linlin(0, 1, 0.5, 0.1),
	\sweep, 1,
	\drive, -10,
	\gain, -3,
)
<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 2, 4])
// <> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 3])
<> ~makeSubdivision.(
	Pseq([1, 2, 1] * 2, 1),
	Pseq(#[4, 2, 1, 2, 2], 1)
)
<> Pbind(\instrument, \kick2)
);

Pdef(\hh, Pbind(
	\amp, 1,
	\freq, 10,
)
<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[2, 4])
// <> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1, 4])
<> ~makeSubdivision.(
	Pseq([2, 2, 1, 1], 1),
	Pseq(#[1, 1, 4], 1)
)
<> Pbind(\instrument, \hh)
);
)

(
t = TempoClock.new(150/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

Pdef(\player,
	Pspawner({| sp |
		var sectionLength = 32;

		\a.postln;
		sp.par(
			Pbindf(
				Pdef(\segPattern),
				\rate, 2,
				\buf, a.at(\file),
				\gain, 0,
				//note b for slices
				\stretch, Pkey(\groupdelta).linlin(0, 1, 1, 1.25),
				\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(3) + Pkey(\sliceStart), b),
				\out, [~mainout, ~convolve_A, ~miVerb]
			).finDur(sectionLength + 2)
		);

		sp.par(
			Pbindf(
				Pdef(\kick0),
				\gain, 6,
				\out, [~filter],
			).finDur(sectionLength)
		);

		/*
		sp.par(
			Pbindf(
				Pdef(\hh),
				\gain, -5,
				\dec, Pkey(\groupdelta) * 0.33,
				\out, [~convolve_B, ~mod1_in],
			).finDur(sectionLength)
		);*/

		sp.par(
			Pbindf(
				Pdef(\stab),
				// \timingOffset, Pkey(\groupcount) / Pkey(\groupdelta),
				\out, [~filter, ~mod1_in],
			).finDur(sectionLength)
		);

		sp.par(
			Pdef(\follower1,
				Pmono(\envfollow,
					\dur, 0.1,
					\atk, 0.01,
					\rel, 0.01,
					\amp, 0.1,
					\mul, 1,
					\add, 1,
					\inbus, ~mod1_in,
					\out, ~mod1_out,
				)
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\filter),
				\dur, 0.1,
				\blend, ~mod1_out,
				\freq, Pseg(Pseq([50, 9000],inf), 4, 'lin' , inf),
				\res, 1,
				\out, [~mainout, ~convolve_B]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, 0,
				\rel, 100,
				\out, [~mainout, ~miVerb]
			).finDur(sectionLength)
		);

		//FX
		sp.par(
			Pbindf(
				Pdef(\grain),
				\bufnum, ~bufC,
				\overlap, 100,
				\trigRate, 500,
				\posRate, 1,
				\rate, 1,
				\recRate, 1,
				\polarityMod, 1,
				\overdub, 0,
				\feedback, 0,
				\gain, -12,
				\out, [~mainout]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\miVerb),
				\time, 0.25,
				\damp, 0.6,
				\hp, 0.0,
				\freeze, 0,
				\diff, 0.9,
				\gain, -16
			)
		);

		sp.wait(sectionLength);
		////////////////////////////////////////////////////////
		\b.postln;
		sectionLength = 64;

		sp.par(
			Pbindf(
				Pdef(\segPattern),
				\rate, 1,
				\buf, a.at(\file),
				\gain, 0,
				//note b for slices
				\stretch, Pkey(\groupdelta).linlin(0, 1, 1, 1.25),
				\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(3) + Pkey(\sliceStart), b),
				\out, [~mainout, ~convolve_A, ~miVerb]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\drumSliced),
				\gain, -3,
				\out, [~mainout, ~grain],
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kick),
				\gain, 6,
				\out, [~filter],
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\hh),
				\gain, -5,
				\dec, (1 - Pkey(\groupdelta)) * 0.33,
				\out, [~mainout, ~convolve_B, ~mod1_in, ~miVerb],
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\stab),
				\out, [~filter, ~mod1_in],
			).finDur(sectionLength)
		);

		sp.par(
			~appendRest.(
				Pbindf(
					Pdef(\modalPatt),
					\stretch, Pkey(\groupdelta).linlin(0, 1, 2, 1),
					\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta),
					\f0, 90,
					\out, [~eq],
					\gain, 6,
				), 8, 0.5
			).finDur(sectionLength)
		);

		sp.par(
			PfadeIn(
				Pbindf(
					Pdef(\kickDrone),
					// \gain, -6,
					\out, [~mainout, ~grain]
			), 32).finDur(sectionLength)
		);

		//FX
		sp.par(
			Pbindf(
				Pdef(\eq),
				\lofreq, 400,
				\lodb, 15,
				\out, [~filter, ~mod1_in]
			).finDur(sectionLength)
		);

		sp.par(
			Pdef(\follower1,
				Pmono(\envfollow,
					\dur, 0.1,
					\atk, 0.01,
					\rel, 0.01,
					\amp, 0.1,
					\mul, 1,
					\add, 1,
					\inbus, ~mod1_in,
					\out, ~mod1_out,
				)
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\filter),
				\dur, 0.1,
				\blend, ~mod1_out,
				\freq, Pseg(Pseq([50, 9000],inf), 4, 'lin' , inf),
				\res, 0.5,
				\out, [~mainout, ~convolve_B]
			).finDur(sectionLength)
		);

		//FX
		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, 0,
				\rel, 100,
				\out, [~mainout, ~miVerb]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\grain),
				\bufnum, ~bufC,
				\overlap, 100,
				\trigRate, 500,
				\posRate, 1,
				\rate, 1,
				\recRate, 1,
				\polarityMod, 1,
				\overdub, 0,
				\feedback, 0,
				\gain, -12,
				\out, [~mainout]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);
		////////////////////////////////////////////////////////
		\c.postln;

		sectionLength = 96;

		sp.par(
			Pbindf(
				Pdef(\segPattern),
				\buf, a.at(\file),
				\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(3) + Pkey(\sliceStart), b),
				\out, [~mainout, ~convolve_A, ~miVerb],
				\rate, 1
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kick),
				\gain, 6,
				\out, [~filter],
			).finDur(sectionLength)
		);


		sp.par(
			Pbindf(
				Pdef(\hh),
				\gain, -5,
				\dec, (1 - Pkey(\groupdelta)) * 0.33,
				\out, [~filter, ~mod1_in, ~miVerb],
			).finDur(sectionLength)
		);


		sp.par(
			Pbindf(
				Pdef(\drumSliced),
				\gain, -3,
				\out, [~mainout, ~grain],
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\stab2),
				\out, [~mainout, ~miVerb],
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\filter),
				\dur, 0.1,
				\blend, ~mod1_out,
				\freq, Pseg(Pseq([50, 9000],inf), 4, 'lin' , inf),
				\res, 0.5,
				\out, [~mainout, ~grain, ~convolve_B]
			).finDur(sectionLength)
		);

		sp.par(
			~appendRest.(
				Pbindf(
					Pdef(\modalPatt),
					// \f0, 180,
					\stretch, Pkey(\groupdelta).linlin(0, 1, 2, 1),
					\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta),
					\out, [~eq],
					\gain, 6,
				), 8, 0.5
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kickDrone),
				\index, Pseg(Pseq([1, 1.5]), 64),
				\out, [~mainout, ~grain]
			).finDur(sectionLength)
		);


		sp.par(
			Pbindf(
				Pdef(\eq),
				\lofreq, 400,
				\lodb, 15,
				\out, [~mainout, ~grain, ~convolve_A]
			).finDur(sectionLength)
		);

		//FX
		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, 0,
				\rel, 10,
				\out, [~mainout, ~miVerb]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\grain),
				\bufnum, ~bufA,
				\overlap, 100,
				\trigRate, 500,
				\posRate, 0.9,
				\rate, 1,
				\recRate, 1,
				\polarityMod, 1,
				\overdub, 0.4,
				\feedback, 0.3,
				\gain, -30,
				\out, [~mainout]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);
		////////////////////////////////////////////////////////
		\d.postln;

		sectionLength = 32;

		sp.par(
			Pbindf(
				Pdef(\segPattern),
				\buf, b.at(\file),
				\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(1) + Pkey(\sliceStart), b),
				\out, [~mainout, ~convolve_A, ~miVerb],
				\rate, 2
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\stab2),
				\out, [~mainout, ~convolve_B, ~miVerb],
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, 6,
				\rel, 100,
				\out, [~mainout]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);
		////////////////////////////////////////////////////////
		\e.postln;

		sectionLength = 64;

		sp.par(
			Pbindf(
				Pdef(\segPattern),
				\buf, b.at(\file),
				\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(1) + Pkey(\sliceStart), b),
				\out, [~mainout, ~convolve_B, ~miVerb],
				\rate, 12.midiratio,
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kick2),
				// \gain, 6,
				\out, [~filter],
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\hh),
				\gain, -5,
				\dec, (1 - Pkey(\groupdelta)) * 0.33,
				\out, [~filter, ~mod1_in, ~miVerb],
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\drumSliced),
				\gain, -3,
				\out, [~mainout, ~grain],
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\stab3),
				\out, [~mainout, ~miVerb],
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\filter),
				\dur, 0.1,
				\blend, ~mod1_out,
				\freq, Pseg(Pseq([50, 9000],inf), 4, 'lin' , inf),
				\res, 0.5,
				\out, [~mainout, ~grain]
			).finDur(sectionLength)
		);

		sp.par(
			~appendRest.(
				Pbindf(
					Pdef(\modalPatt),
					// \f0, 180,
					\stretch, Pkey(\groupdelta).linlin(0, 1, 2, 1),
					// \timingOffset, Pkey(\groupcount) / Pkey(\groupdelta),
					\out, [~eq, ~convolve_A],
					\gain, 6,
				), 8, 0.5
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kickDrone),
				\index, 1.75,
				\out, [~mainout, ~grain]
			).finDur(sectionLength)
		);


		sp.par(
			Pbindf(
				Pdef(\eq),
				\lofreq, 400,
				\lodb, 15,
				\out, [~mainout, ~grain]
			).finDur(sectionLength)
		);

		//FX
		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, 0,
				\rel, 10,
				\out, [~mainout, ~miVerb]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\grain),
				\bufnum, ~bufA,
				\overlap, 100,
				\trigRate, 100,
				\posRate, 1,
				\rate, 1,
				\recRate, 1,
				\polarityMod, 1,
				\overdub, 0.4,
				\feedback, 0,
				\gain, -17,
				\out, [~mainout]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);
		////////////////////////////////////////////////////////
/*		\f.postln;

		sectionLength = 64;

		sp.par(
			Pbindf(
				Pdef(\segPattern),
				\buf, b.at(\file),
				\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(1) + Pkey(\sliceStart), b),
				\out, [~mainout, ~convolve_B, ~miVerb],
				\rate, Pseq([11.midiratio, 12.midiratio], inf).stutter(64),
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kick3),
				// \gain, 6,
				\out, [~filter],
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\hh),
				\gain, -5,
				\dec, (1 - Pkey(\groupdelta)) * 0.33,
				\out, [~filter, ~mod1_in, ~miVerb],
			).finDur(sectionLength)
		);


		sp.par(
			Pbindf(
				Pdef(\drumSliced),
				\gain, -3,
				\out, [~mainout, ~grain],
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\stab4),
				\out, [~filter, ~mod1_in, ~miVerb],
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\filter),
				\dur, 0.1,
				\blend, ~mod1_out,
				\freq, Pseg(Pseq([50, 9000],inf), 4, 'lin' , inf),
				\res, 0.5,
				\out, [~mainout, ~grain]
			).finDur(sectionLength)
		);

		sp.par(
			~appendRest.(
				Pbindf(
					Pdef(\modalPatt),
					// \f0, 180,
					\stretch, Pkey(\groupdelta).linlin(0, 1, 2, 1),
					// \timingOffset, Pkey(\groupcount) / Pkey(\groupdelta),
					\out, [~eq, ~convolve_A],
					\gain, 6,
				), 8, 0.5
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kickDrone),
				\index, Pseg(Pseq([1.75, 1]), 64),
				\out, [~mainout, ~grain]
			).finDur(sectionLength)
		);


		sp.par(
			Pbindf(
				Pdef(\eq),
				\lofreq, 400,
				\lodb, 15,
				\out, [~mainout, ~grain]
			).finDur(sectionLength)
		);

		//FX
		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, 0,
				\rel, 10,
				\out, [~mainout, ~miVerb]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\grain),
				\bufnum, ~bufA,
				\overlap, 100,
				\trigRate, 100,
				\posRate, 1,
				\rate, 1,
				\recRate, 1,
				\polarityMod, 1,
				\overdub, 0.4,
				\feedback, 0,
				\gain, -17,
				\out, [~mainout]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);*/
		////////////////////////////////////////////////////////
		\g.postln;

		sectionLength = 32;

		sp.par(
			Pbindf(
				Pdef(\segPattern),
				\tempo, 75/60,
				\buf, b.at(\file),
				\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(1) + Pkey(\sliceStart), b),
				\out, [~mainout, ~convolve_B, ~miVerb],
				\rate, 2,
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kick2),
				// \gain, 6,
				\out, [~filter],
			).finDur(sectionLength)
		);


		sp.par(
			Pbindf(
				Pdef(\hh),
				\gain, -5,
				\dec, (1 - Pkey(\groupdelta)) * 0.33,
				\out, [~filter, ~mod1_in, ~miVerb],
			).finDur(sectionLength)
		);


		sp.par(
			Pbindf(
				Pdef(\drumSliced),
				\gain, -3,
				\out, [~mainout, ~grain],
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\stab3),
				\out, [~mainout, ~miVerb],
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\filter),
				\dur, 0.1,
				\blend, ~mod1_out,
				\freq, Pseg(Pseq([50, 9000],inf), 4, 'lin' , inf),
				\res, 0.5,
				\out, [~mainout, ~grain]
			).finDur(sectionLength)
		);

		sp.par(
			~appendRest.(
				Pbindf(
					Pdef(\modalPatt),
					// \f0, 180,
					\stretch, Pkey(\groupdelta).linlin(0, 1, 2, 1),
					// \timingOffset, Pkey(\groupcount) / Pkey(\groupdelta),
					\out, [~eq, ~convolve_A],
					\gain, 6,
				), 8, 0.5
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kickDrone),
				\index, 1.75,
				\out, [~mainout, ~grain]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\eq),
				\lofreq, 400,
				\lodb, 15,
				\out, [~mainout, ~grain]
			).finDur(sectionLength)
		);

		//FX
		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, 0,
				\rel, 10,
				\out, [~mainout, ~miVerb]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\grain),
				\bufnum, ~bufA,
				\overlap, 100,
				\trigRate, 100,
				\posRate, 1,
				\rate, 1,
				\recRate, 1,
				\polarityMod, 1,
				\overdub, 0.4,
				\feedback, 0,
				\gain, -17,
				\out, [~mainout]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);
		////////////////////////////////////////////////////////
		\h.postln;

		sp.par(
			Pbindf(
				Pdef(\segPattern),
				\rate, 2,
				\buf, a.at(\file),
				\gain, 0,
				//note b for slices
				\stretch, Pkey(\groupdelta).linlin(0, 1, 1, 1.25),
				\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(3) + Pkey(\sliceStart), b),
				\out, [~mainout, ~convolve_A, ~miVerb]
			).finDur(sectionLength + 2)
		);

		//FX
		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, 0,
				\rel, 10,
				\out, [~mainout, ~miVerb]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);
		////////////////////////////////////////////////////////
		\i.postln;
		sp.par(
			Pbindf(
				Pdef(\grain),
				\bufnum, ~bufA,
				\overlap, 100,
				\trigRate, 100,
				\posRate, 1,
				\rate, 1,
				\recRate, 1,
				\polarityMod, 1,
				\overdub, 0.4,
				\feedback, 0,
				\gain, -17,
				\out, [~mainout]
			).finDur(sectionLength)
		);

	})
).play(t);
)
