s.boot
Pdef.clear;


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
a = Dictionary();
~analyzeSlices.(
"/Users/aelazary/Desktop/Samples etc./Field recs/Rock song.wav"
	, a, 0.2, \centroid);

b = Dictionary();
~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./hollywood edge - foley sound library/FSL-05/FSL-05-Money Counting ; Pull Money Bills From Clip, Rapid Counting, And Place Into Pocket. - Counting Money.wav", b, 0.2, \centroid);

c = Dictionary();
~analyzeSlices.(
"/Users/aelazary/Desktop/Samples etc./hollywood edge - foley sound library/FSL-05/FSL-05-Rocks Grind And Impact ; Rocks Scraping And Movement. - Scrape Rock.wav"
	, c, 0.2, \centroid);

d = Dictionary();
~analyzeSlices.(
	"/Users/aelazary/Desktop/Samples etc./NEW sample lib/BELLS/Ringing the Peace Bell-F0kitWohMbo.wav"
	, d, 0.1, \centroid);
)


Pdef.clear;

(

Pdef(\p1,
~makeSubdivision.(
	PlaceAll([[1, 1.5], [1.5, Rest(1.5)], 1, [Rest(0.5), 1]], inf),
	PlaceAll([[4, 1], 4, 0], inf)
	)
);

/*Pdef(\p1,
 	~makeSubdivision.(
		PlaceAll([[1.5, Rest(1.5)], 1.5, 1, Rest(1)], inf),
 		PlaceAll([[4, 0, 5], [4, 5, 0], [0, 4, 3], [4, 0, 3]], inf)
 	)
);*/

Pdef(\sample, Pbind(
	\amp, 1,
	\dec, Pkey(\dur) * 1,
	\buf, a.at(\file),
	\sliceStart, 0,
	\sliceStutter, 5,
	// \slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), a),
	 \slice, ~pGetSlice.(Pkey(\eventcount).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), a),
	//\pan, 0,
	\atk, 0.1,
	// \rel, 1.5,
	// \rel, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 1, 2, 0.75), Pkey(\dec), 1),
	\curve, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, -8, 0), Pkey(\dec), 1),
	\rate, 0.5,
	// \gain, 12,
	\gain, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 9, 3, -4), Pkey(\dec), 1),
	//pitchshifter
	\pitchMix, 0.5,
	\pitchRatio, 2,
	\windowSize, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.01, 0.25), Pkey(\dec), 1, \sine),
	\pitchDispersion, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.1, 0.01), Pkey(\dec), 1, \linear),
	\timeDispersion, 0.01,
);
);

Pdef(\sample2, Pbind(
	\amp, 1,
	\dec, Pkey(\dur) * 1,
	\buf, c.at(\file),
	\sliceStart, 60,
	\sliceStutter, 1,
	\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), c),
	//\pan, 0,
	\atk, 0.01,
	// \rel, 1.5,
	\rel, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 1, 2, 0.75), Pkey(\dec), 1),
	\curve, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, -8, 0), Pkey(\dec), 1),
	\rate, 0.5,
	// \gain, 12,
	\gain, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 9, 3, -4), Pkey(\dec), 1),
	//pitchshifter
	\pitchMix, 0.5,
	\pitchRatio, 2,
	\windowSize, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.01, 0.25), Pkey(\dec), 1, \sine),
	\pitchDispersion, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.1, 0.01), Pkey(\dec), 1, \linear),
	\timeDispersion, 0.01,
);
);

Pdef(\sample3, Pbind(
	\amp, 1,
	\dec, Pkey(\dur) * 1,
	\buf, b.at(\file),
	\sliceStart, 60,
	\sliceStutter, 1,
	\slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), b),
	//\pan, 0,
	\atk, 0.01,
	// \rel, 1.5,
	\rel, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 1, 2, 0.75), Pkey(\dec), 1),
	\curve, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, -8, 0), Pkey(\dec), 1),
	\rate, 0.5,
	// \gain, 12,
	\gain, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 9, 3, -4), Pkey(\dec), 1),
	//pitchshifter
	\pitchMix, 0.5,
	\pitchRatio, 2,
	\windowSize, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.01, 0.25), Pkey(\dec), 1, \sine),
	\pitchDispersion, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.1, 0.01), Pkey(\dec), 1, \linear),
	\timeDispersion, 0.01,
);
);

Pdef(\seq1,
Pdef(\sample)
	<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4], mod: 5)
<> Pdef(\p1)
<> Pbind(\instrument, \segPlayer)
);

Pdef(\seq2,
Pdef(\sample2)
	<> ~filterBeat.(key: Pkey(\eventcount), beat:[2, 4], mod: 3)
<> Pdef(\p1)
<> Pbind(\instrument, \segPlayer)
);

Pdef(\seq3,
Pdef(\sample3)
	<> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3], mod: 5)
<> Pdef(\p1)
<> Pbind(\instrument, \segPlayer)
);

Pdef(\kick,
Pbind(
	\amp, 1,
	\dec, 0.8,
	\freq, 40,
	\drive, -12,
	\dec, Pkey(\bardelta).linlin(0, 1, 0.8, 0.2),
		// \dec, 0.8
	)
	<> ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 4)
	<> ~filterBeat.(key: Pkey(\eventcount), beat:[1])
<> Pdef(\p1)
	<> Pbind(\instrument, \simpleSub)
);

Pdef(\padDrone,
	Pmono(\padKlank_mono,
		\f, 15,
		\harmonicRatio, 0.9,
		\bw, 1,
		\bwScale, 1,
		\stretch, 1,
		\detuneRate, 800,
		\detuneDepth, 100,
		\impulse, 20000,
		\dev, 1
	)
);

Pdef(\padDrone2,
	Pmono(\padKlank_mono,
		\f, 30,
		\harmonicRatio, 1,
		\bw, 0.1,
		\bwScale, 1,
		\stretch, 1,
		\detuneRate, 800,
		\detuneDepth, 0,
		\impulse, 20000,
		\dev, 1
	)
);

Pdef(\padDrone3,
	Pmono(\padKlank_mono,
		\f, 30,
		\harmonicRatio, 1,
		\bw, 1,
		\bwScale, 1,
		\stretch, 1,
		\detuneRate, 800,
		\detuneDepth, 1000,
		\impulse, 20000,
		\dev, 1
	)
);
)

(
t = TempoClock.new(140/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});
Pdef(\player,
	Pspawner({| sp |
		var sectionLength = 32;

		\a.postln;
		//reverb
		sp.par(
			Pbindf(
				Pdef(\miVerb),
				\time, 0.25,
				\damp, 0.6,
				\hp, 0.0,
				\freeze, 0,
				\diff, 0.9,
				\gain, -24
			)
		);

/*		sp.par(
			Pbindf(
				Pdef(\padDrone),
				\out, [~convolve_B, ~grain]
			).finDur(sectionLength)
		);*/

		sp.par(
			Pbindf(
				Pdef(\seq1,
					Pdef(\sample)
					<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4], mod: 5)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\sliceStart, 0,
				\out, [~mainout, ~convolve_A]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq2,
					Pdef(\sample2)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[2, 4], mod: 3)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\out, [~convolve_A, ~miVerb, ~grain]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq3,
					Pdef(\sample3)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3], mod: 5)
					<> Pdef(\p1)
					<> Pbind(\instrument, \segPlayer)
				),
				\sliceStart, 0,
				\out, [~convolve_B, ~miVerb]
			).finDur(sectionLength)
		);

/*		sp.par(
			Pbindf(
				Pdef(\seq1),
				\sliceStart, 0,
				\out, [~mainout, ~convolve_A]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq3),
				\sliceStart, 0,
				\out, [~convolve_B, ~miVerb]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq2),
				\out, [~convolve_A, ~miVerb, ~grain]
			).finDur(sectionLength)
		);*/

		sp.par(
		 	Pbindf(
		 		Pdef(\grain),
		 		\bufnum, ~bufC,
		 		\overlap, 50,
		 		\trigRate, 500,
		 		\posRate, 1,
		 		\rate, 1,
		 		\recRate, 1,
		 		\polarityMod, 1,
		 		\overdub, 0,
		 		\feedback, 0.4,
		 		\gain, -24,
		 		\out, [~mainout, ~convolve_A]
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

		sp.wait(sectionLength);
		///////////////////////////////
		\b.postln;
		sectionLength=64;

		sp.par(
			Pbindf(
				Pdef(\padDrone),
				\out, [~convolve_B, ~grain]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq1),
				\sliceStart, 0,
				\out, [~mainout, ~convolve_A]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq3),
				\sliceStart, 0,
				\out, [~convolve_B, ~miVerb]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq2),
				\out, [~convolve_A, ~miVerb, ~grain]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kick),
				\out, [~mainout, ~convolve_A, ~miVerb]
			).finDur(sectionLength)
		);

		sp.par(
		 	Pbindf(
		 		Pdef(\grain),
		 		\bufnum, ~bufC,
		 		\overlap, 50,
		 		\trigRate, 500,
		 		\posRate, 1,
		 		\rate, 1,
		 		\recRate, 1,
		 		\polarityMod, 1,
		 		\overdub, 0,
		 		\feedback, 0.4,
		 		\gain, -24,
		 		\out, [~mainout, ~convolve_A]
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

		sp.wait(sectionLength);
		///////////////////////////////
		\c.postln;
		sectionLength=64;

		sp.par(
			Pbindf(
				Pdef(\padDrone),
				\out, [~convolve_B, ~grain]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq1),
				\sliceStart, 0,
				\out, [~mainout, ~convolve_A]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq3),
				\sliceStart, 0,
				\out, [~convolve_B, ~miVerb]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq2),
				\out, [~convolve_A, ~miVerb, ~grain]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kick),
				\out, [~mainout, ~convolve_A, ~miVerb]
			).finDur(sectionLength)
		);

		sp.par(
		 	Pbindf(
		 		Pdef(\grain),
		 		\bufnum, ~bufC,
		 		\overlap, 50,
		 		\trigRate, 500,
		 		\posRate, 1,
		 		\rate, 1,
		 		\recRate, 1,
		 		\polarityMod, 1,
		 		\overdub, 0,
		 		\feedback, 0.4,
		 		\gain, -24,
		 		\out, [~mainout, ~convolve_A]
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

	})
).play(t);
)