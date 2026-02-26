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
b = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./NEW sample lib/Star Wars Jedi Knight Jedi Academy - All Sound Effects/player/fallsplat.wav");

c = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill 3/Missionary Battle/THNDOFDKNS.wav");

// d = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./NEW sample lib/foodmans_ultra_ancient_civilization_sample_pack/voice/kurushii_voice.wav");

d = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./EchoThiefImpulseResponseLibrary/Underground/FortWordenTunnel.wav");
)

(
~specBuff=Dictionary();
~specBuff2=Dictionary();
// ~makeSpec.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/mother and daughter singing o magnum mysterium-sfLDOVcK7nU.wav", ~specBuff, 16384, 2)
~makeSpec.("/Users/aelazary/Desktop/Samples etc./spannerGuitar/spannerDrumGuitar-4.wav", ~specBuff, 16384, 2);
~makeSpec.("/Users/aelazary/Desktop/Samples etc./spannerGuitar/spannerDrumGuitar-5.wav", ~specBuff2, 16384, 2);
)

(
a = Dictionary();
~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/mother and daughter singing o magnum mysterium-sfLDOVcK7nU.wav", a, 0.2, \crest);
)

(
g.free;
// g = Group.new(RootNode(Server.default), \addToTail);

/*Synth(\pitchShift,
	[
		\pitchRatio: 0.5,
		\windowSize: 0.05,
		\drywet: 1,
		pitchDispersion: 0.0001,
		timeDispersion: 1,
	],
	target: g,
	addAction: \addToTail,
	);

Synth(\tape,
	[
		\pregain: -8,
		\sigmoid, 0.5,
		\drywet: 0,
		\autogain: 1
		],
		target: g,
		addAction: \addToTail,
);*/

// Synth(\comp,
// 	[
// 		\ratio: 6,
// 		\thresh, -40,
// 		\atk, 0.1,
// 		\rel, 1000,
// 		\makeup, 0,
// 		\automakeup, 0
// 	],
//     target: g,
//     addAction: \addToTail,
// );

t = TempoClock.new(130/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});
Pdef(\player,
	Pspawner({| sp |
		var sectionLength = 32;

		//fx const
		sp.par(
			Pbindf(
				Pdef(\miVerb),
				\time, 0.6,
				\damp, 0.6,
				\hp, 0.0,
				\freeze, 0,
				\diff, 0.9,
				\gain, -24,
				\out, ~mainout
			)
		);
		
		// \a.postln;
		// sp.par(
		// 	Pmono(\grainSlicer_mono,
		// 		\amp, 1,
		// 		\buf, a.at(\file),
		// 		\overlap, 100,
		// 		\trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
		// 		\slice, ~pGetSlice.(51, a),
		// 		\gain, -6,
		// 		\posRate, 0.1,
		// 		\rate, 0.25,
		// 		\out, [~convolve_B]
		// 	).finDur(sectionLength)
		// );

		// sp.par(
		// 	Pbindf(
		// 		Pmono(\convolved_pulsar_mono),
		// 		\buf, d,
		// 		\window, 4096,
		// 		\triggerRate, 14,
		// 		\fluxMF, 1.5,
		// 		\fluxMD, 1,
		// 		\grainFreq, 41,
		// 		\overlap, 0.1,
		// 		\pmRatio, 0.24,
		// 		\pmIndex, 100,
		// 		\density, 1,
		// 		\polarityMod, 1,
		// 		\gain, -18,
		// 		\out, [~convolve_A, ~convolve_B]
		// 	).finDur(sectionLength)
		// );

		// sp.par(
		// 	Pbindf(
		// 		Pdef(\morph),
		// 		\gain, -0,
		// 		\atk, 500,
		// 		\rel, 100,
		// 		// \swap, ~pmodenv.(Pseq([0, 0.1],inf), Pseq([4], inf)),
		// 		\swap, 0,
		// 		\out, [~mainout, ~miVerb]
		// 	).finDur(sectionLength)
		// );

		// sp.wait(sectionLength);
		////////////////////////////////////////////
		sectionLength = 64;
		\b.postln;

		sp.par(
			Pmono(\grainSlicer_mono,
				\amp, 1,
				\buf, a.at(\file),
				\overlap, 100,
				\trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
				\slice, ~pGetSlice.(51, a),
				\gain, -6,
				\posRate, 0.1,
				\rate, 0.25,
				\out, [~convolve_B]
			).finDur(sectionLength)
		);

		sp.par(
			PfadeIn(
			Pmono(\fftStretch_magAbove_mono,
				\amp, 1,
				\gain, 0,
				\buf, ~specBuff.at(\file),
				\analysis, [~specBuff.at(\analysis)],
				\fftSize, ~specBuff.at(\fftSize),
				\rate, 0.01,
				\pos, 0.6,
				\len, 0.05,
				\filter, ~pmodenv.(Pseq([1, 100],inf), Pseq([4, 8, 4], inf), curve: \sine),
				\out, [~mainout]
			),
			16).finDur(sectionLength)
		);


		Pdef(\p1,
			~pFade.(
				Pdef(\p1_b,
					~makeSubdivision.(
						PlaceAll([Rest(8)], inf),
						PlaceAll([8], inf)
					)
				),

				Pdef(\p1_a,
					~makeSubdivision.(
						PlaceAll([[1, 3], Pxrand((1..3), inf), [3, Rest(1)], 4 ], inf),
						PlaceAll([[Pxrand((1..4), inf), 1], 0, 4], inf)
					)
				),
				8, loop:1
			)
		);

		sp.par(
			Pbindf(
				Pmono(\convolved_pulsar_mono),
				\buf, b,
				\window, 4096,
				\triggerRate, 14,
				\fluxMF, 1.5,
				\fluxMD, 1,
				\grainFreq, 41,
				\overlap, 0.1,
				\pmRatio, 0.24,
				\pmIndex, 100,
				\density, 1,
				\polarityMod, 1,
				\gain, -18,
				\out, [~convolve_A]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pmono(\pulsar_mono),
				\triggerRate, 120/60/8 * 0.5,
				\fluxMF, 1.5,
				\fluxMD, ~pmodenv.(Pseq([100, 30],inf), Pseq([4],inf)),
				\grainFreq, 120/60/4,
				\overlap, 1,
				\pmRatio, 100,
				\pmIndex, 3,
				\density, 1,
				\polarityMod, 1,
				\out, [~convolve_B]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kick,
					Pbind(
						\freq, 40,
						\amp, 3,
						\dec, 1.5,
						\fb, ~pmodenv.(Pseq([0.5, 1.2, 0.5],inf), Pkey(\dur)),
						\index, 0.5,
					)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[1])
					<> Pdef(\p1)
					<> Pbind(\instrument, \fmKick)
				),
				\out, [~mainout, ~convolve_A]
			).finDur(sectionLength)
		);

		Pdef(\fb1mod,
			Pbind(
				\amp, 0.7,
				\dur, Pwhite(0.125, 0.5, inf).stutter(4),
				\dec, Pkey(\dur) * 1,
				\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
				\time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 1),inf), Pkey(\dec)),
				\damp, ~pmodenv.(Pseq([0.1, 1],inf), Pkey(\dec)),
				\exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec), 1, Pseq([\sine],inf)),
				\impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pkey(\dec)),
				\spont, ~pmodenv.(Pseq([60,1000],inf), Pkey(\dec)),
				\boost,  ~pmodenv.(Pseq([20000,200],inf), Pkey(\dec)),
				\restore, 40,
				\dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
				\rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
				\pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
				\gain, 0,
			)
		);

		sp.par(
			Pbindf(
				Pdef(\seq3,
					Pdef(\fb1mod)
					<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4, 7], mod: 7)
					<> Pdef(\p1)
					<> Pbind(\instrument, \fb1)
				),
				\out, [~mainout, ~convolve_B, ~miVerb]
			).finDur(sectionLength)
		);

		Pdef(\fb2Mod,
			Pbind(
				\amp, 0.05,
				\buf, b,
				\window, 1024,
				\dec, Pkey(\dur),
				\sustainTime, Pkey(\dur),
				// \exciter, 0.6,
				// \density, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
				\exciter, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
				\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pseq([4, 8, 2, 4],inf)),

				\time, ~pmodenv.(Pseq([0.005, 0.001, 0.010],inf) * 1 * Pkey(\groupdelta), Pkey(\dec)),
				\damp, ~pmodenv.(Pseq([1, 0],inf), Pwhite(0.01, 0.4, inf)),
				// \damp, ~pmodenv.(Pkey(\cycledelta) * 1, Pkey(\dec)),

				\filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),

				\delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5, 0.25], inf)),

				\impulse, ~pmodenv.(Pwhite(5600, 20000, inf), Pkey(\dec)),
				\rev, ~pmodenv.(Pexprand(0.1, 10, inf), Pseq([2, 4], inf)),

				\pan, ~pmodenv.(Pwhite(-0.8, 0.8, inf), Pseq([0.5, 0.25],inf)),
			)
		);

		sp.par(
			Pbindf(
				Pdef(\seq1,
					Pdef(\fb2Mod)
					<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4], mod: 5)
					<> Pdef(\p1)
					<> Pbind(\instrument, \fb2)
				),
				\out, [~mainout]
			).finDur(sectionLength)
		);

		Pdef(\fmPercMod,
			Pbind(
				\amp, 1,
				\freq, 20.midicps,
				//\dur, 0.5,
				\dec, Pkey(\dur) * 1,
				//\atk,0,
				\atk, ~pmodenv.(Pkey(\groupdelta) * 0.5, Pkey(\dec)),
				\rel, ~pmodenv.(Pwhite(1, 0.25, inf), Pkey(\dec)),
				\rel, Pkey(\dec),
				\feedback, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
				\fbmod, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
				\carRatio, 4,
				\m1Ratio, 4,
				\m2Ratio, 2,
				\m3Ratio, 1.5,
				\index1, ~pmodenv.(Pexprand(0.01, 1, inf), Pkey(\dec)),
				\index2, ~pmodenv.(Pexprand(0.01, 1, inf), Pkey(\dec)),
				\iscale, ~pmodenv.((1 - Pkey(\cycledelta)) + 1e-4, Pkey(\dec)),
				\spread, ~pmodenv.(Pwhite(10, 30, inf), Pkey(\dec)),
				\mix, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
				// \mix, 1,
				\drive, 16,
				// \drive, ~pmodenv.(Pkey(\groupdelta) * 16, Pkey(\dec)),
				\gain, -3,
			)
		);

		sp.par(
			Pbindf(
				Pdef(\seq2,
					Pdef(\fmPercMod)
					// <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4], mod: 3)
					<> Pdef(\p1)
					<> Pbind(\instrument, \fmPerc)
					),
				\out, [~convolve_A]
		).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, -18,
				\atk, 500,
				\rel, 100,
				// \swap, ~pmodenv.(Pseq([0, 0.1],inf), Pseq([4], inf)),
				\swap, 0,
				\out, [~mainout, ~miVerb]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);
		////////////////////////////////////////////
		sectionLength = 32;

		\c.postln;
		Pdef(\p1,
			~pFade.(
				Pdef(\p1_a,
					~makeSubdivision.(
						PlaceAll([[1, 3], Pxrand((1..3), inf), [3, Rest(1)], 4 ], inf),
						PlaceAll([[Pxrand((1..4), inf), 1], 0, 4], inf)
					)
				),

				Pdef(\p1_b,
					~makeSubdivision.(
						PlaceAll([4, Rest(4)], inf),
						PlaceAll([2, 2], inf)
					)
				),
				8, loop:1
			)
		);

		sp.par(
			Pmono(\grainSlicer_mono,
				\amp, 1,
				\buf, a.at(\file),
				\overlap, 100,
				\trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
				\slice, ~pGetSlice.(51, a),
				\gain, -6,
				\posRate, 0.1,
				\rate, 0.25,
				\out, [~convolve_B]
			).finDur(sectionLength)
		);

		sp.par(
			Pmono(\fftStretch_magAbove_mono,
				\amp, 1,
				\gain, 0,
				\buf, ~specBuff.at(\file),
				\analysis, [~specBuff.at(\analysis)],
				\fftSize, ~specBuff.at(\fftSize),
				\rate, 0.01,
				\pos, 0.5,
				\len, 0.05,
				\filter, ~pmodenv.(Pseq([1, 100],inf), Pseq([4, 8, 4], inf), curve: \sine),
				\out, [~mainout, ~convolve_B]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pmono(\convolved_pulsar_mono),
				\buf, d,
				\window, 4096,
				\triggerRate, 14,
				\fluxMF, 1.5,
				\fluxMD, 1,
				\grainFreq, 41,
				\overlap, 0.1,
				\pmRatio, 0.24,
				\pmIndex, 100,
				\density, 1,
				\polarityMod, 1,
				\gain, -18,
				\out, [~convolve_A, ~convolve_B]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pmono(\pulsar_mono),
				\triggerRate, 120/60/8,
				\fluxMF, 1.5,
				\fluxMD, ~pmodenv.(Pseq([30, 15],inf), Pseq([6],inf)),
				\grainFreq, 120/60/4,
				\overlap, 1000,
				\pmRatio, 100,
				\pmIndex, 3,
				\density, 1,
				\polarityMod, 1,
				\out, [~mainout, ~convolve_A]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kick,
					Pbind(
						\freq, 40,
						\amp, 3,
						\dec, 1.5,
						\fb, ~pmodenv.(Pseq([0.5, 1.2, 0.5],inf), Pkey(\dur)),
						\index, 0.5,
					)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[1])
					<> Pdef(\p1)
					<> Pbind(\instrument, \fmKick)
				),
				\out, [~mainout, ~convolve_A]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq3,
					Pdef(\fb1Mod)
					<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4, 7], mod: 7)
					<> Pdef(\p1)
					<> Pbind(\instrument, \fb1)
				),
				\out, [~mainout, ~convolve_B, ~miverb]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq1,
					Pdef(\fb2Mod)
					<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4], mod: 5)
					<> Pdef(\p1)
					<> Pbind(\instrument, \fb2)
				),
				\out, [~mainout]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq2,
					Pdef(\fmPercMod)
					// <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4], mod: 3)
					<> Pdef(\p1)
					<> Pbind(\instrument, \fmPerc)
					),
				\out, [~convolve_A]
		).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, -18,
				\atk, 500,
				\rel, 100,
				\swap, 0,
				\out, [~mainout, ~miVerb]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);
		////////////////////////////////////////////

		\c.postln;
		sectionLength = 64;

		Pdef(\p1,
			~pFade.(
				Pdef(\p1_a,
					~makeSubdivision.(
						PlaceAll([[1, 3], Pxrand((1..3), inf), [3, Rest(1)], 4 ], inf),
						PlaceAll([[Pxrand((1..4), inf), 1], 0, 4], inf)
					)
				),

				Pdef(\p1_b,
					~makeSubdivision.(
						PlaceAll([1, Rest(1), 1, 4], inf),
						PlaceAll([4, 4, 4, 4], inf)
					)
				),

				16, loop:1
			)
		);

		sp.par(
			Pmono(\grainSlicer_mono,
				\amp, 1,
				\buf, a.at(\file),
				\overlap, 100,
				\trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
				\slice, ~pGetSlice.(51, a),
				\gain, -12,
				\posRate, 0.1,
				\rate, 0.5,
				\out, [~convolve_B]
			).finDur(sectionLength)
		);

		sp.par(
			Pmono(\fftStretch_magAbove_mono,
				\amp, 1,
				\gain, 0,
				\buf, ~specBuff.at(\file),
				\analysis, [~specBuff.at(\analysis)],
				\fftSize, ~specBuff.at(\fftSize),
				\rate, 0.2,
				\pos, 0.3,
				\len, 0.4,
				\filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
				\out, [~mainout, ~convolve_B]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pmono(\driftingSines_mono),
				\amp, 0.1,
				\gain, -6,
				\freq, 200,
				// \freq, ~pmodenv.(Pseq([150, 400],inf), Pxrand([0.5, 0.25, 0.125] * 2, inf)),
				\numharm, 31,
				\pitchDev, 3,
				\out, [~convolve_B]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pmono(\convolved_pulsar_mono),
				\buf, b,
				\window, 4096,
				\triggerRate, 14,
				\fluxMF, 1.5,
				\fluxMD, 1,
				\grainFreq, 41,
				\overlap, 0.1,
				\pmRatio, 0.24,
				\pmIndex, 100,
				\density, 1,
				\polarityMod, 1,
				\gain, -18,
				\out, [~convolve_A, ~convolve_B]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pmono(\pulsar_mono),
				\triggerRate, 120/60/8 * 0.5,
				\fluxMF, 1.5,
				\fluxMD, ~pmodenv.(Pseq([30, 15],inf), Pseq([6],inf)),
				\grainFreq, 120/60/4,
				\overlap, 1000,
				\pmRatio, 100,
				\pmIndex, 3,
				\density, 1,
				\polarityMod, 1,
				\out, [~mainout, ~convolve_A]
			).finDur(sectionLength)
		);


		sp.par(
			Pbindf(
				Pdef(\kick,
					Pbind(
						\freq, 40,
						\amp, 3,
						\dec, 1.5,
						\fb, ~pmodenv.(Pseq([0.5, 1.2, 0.5],inf), Pkey(\dur)),
						\index, 0.5,
					)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[1])
					<> Pdef(\p1)
					<> Pbind(\instrument, \fmKick)
				),
				\out, [~mainout, ~convolve_A]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq3,
					Pdef(\fb1Mod)
					<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4, 7], mod: 7)
					<> Pdef(\p1)
					<> Pbind(\instrument, \fb1)
				),
				\out, [~mainout, ~convolve_B, ~miverb]
			).finDur(sectionLength)
		);

		Pdef(\fb2Mod,
			Pbind(
				\amp, 0.05,
				\buf, b,
				\window, 1024,
				\dec, Pkey(\dur),
				\sustainTime, Pkey(\dur),
				// \exciter, 0.6,
				// \density, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
				\exciter, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
				\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pseq([4, 8, 2, 4],inf)),

				\time, ~pmodenv.(Pseq([0.005, 0.001, 0.010],inf) * 1 * Pkey(\groupdelta), Pkey(\dec)),
				\damp, ~pmodenv.(Pseq([1, 0],inf), Pwhite(0.01, 0.4, inf)),
				// \damp, ~pmodenv.(Pkey(\cycledelta) * 1, Pkey(\dec)),

				\filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),

				\delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5, 0.25], inf)),

				\impulse, ~pmodenv.(Pwhite(5600, 20000, inf), Pkey(\dec)),
				\rev, ~pmodenv.(Pexprand(0.1, 10, inf), Pseq([2, 4], inf)),

				\pan, ~pmodenv.(Pwhite(-0.8, 0.8, inf), Pseq([0.5, 0.25],inf)),
			)
		);

		sp.par(
			Pbindf(
				Pdef(\seq1,
					Pdef(\fb2Mod)
					<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4], mod: 5)
					<> Pdef(\p1)
					<> Pbind(\instrument, \fb2)
				),
				\out, [~mainout]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq2,
					Pbind(
						\amp, 1,
						\freq, 20.midicps,
						//\dur, 0.5,
						\dec, Pkey(\dur) * 1,
						//\atk,0,
						\atk, ~pmodenv.(Pkey(\groupdelta) * 0.5, Pkey(\dec)),
						\rel, ~pmodenv.(Pwhite(1, 0.25, inf), Pkey(\dec)),
						\rel, Pkey(\dec),
						\feedback, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
						\fbmod, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
						\carRatio, 4,
						\m1Ratio, 4,
						\m2Ratio, 2,
						\m3Ratio, 1.5,
						\index1, ~pmodenv.(Pexprand(0.01, 1, inf), Pkey(\dec)),
						\index2, ~pmodenv.(Pexprand(0.01, 1, inf), Pkey(\dec)),
						\iscale, ~pmodenv.((1 - Pkey(\cycledelta)) + 1e-4, Pkey(\dec)),
						\spread, ~pmodenv.(Pwhite(10, 30, inf), Pkey(\dec)),
						\mix, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
						// \mix, 1,
						\drive, 16,
						// \drive, ~pmodenv.(Pkey(\groupdelta) * 16, Pkey(\dec)),
						\gain, -3,
						)
					// <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4], mod: 3)
					<> Pdef(\p1)
					<> Pbind(\instrument, \fmPerc)
					),
				\out, [~convolve_A]
		).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, -18,
				\atk, 500,
				\rel, 100,
				// \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([4], inf)),
				\swap, 0,
				\out, [~mainout, ~miVerb]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);
		////////////////////////////////////////////

		\d.postln;
		sectionLength = 64;

		sp.par(
			Pmono(\grainSlicer_mono,
				\amp, 1,
				\buf, a.at(\file),
				\overlap, 100,
				\trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
				\slice, ~pGetSlice.(51, a),
				\gain, -12,
				\posRate, 0.1,
				\rate, 0.5,
				\out, [~convolve_B, ~miVerb]
			).finDur(sectionLength)
		);

		Pdef(\p1,
			~pFade.(
				Pdef(\p1_a,
					~makeSubdivision.(
						PlaceAll([[1, 3], Pxrand((1..3), inf), [3, Rest(1)], 4 ], inf),
						PlaceAll([[Pxrand((1..4), inf), 1], 1, 4], inf)
					)
				),

				Pdef(\p1_b,
					~makeSubdivision.(
						PlaceAll([1, Rest(1), 1, 4], inf),
						PlaceAll([4, 4, 4, 4], inf)
					)
				),

				16, loop:1
			)
		);

		sp.par(
			Pbindf(
				Pmono(\driftingSines_mono),
				\amp, 0.1,
				\gain, -18,
				\freq, 400,
				// \freq, ~pmodenv.(Pseq([150, 400],inf), Pxrand([0.5, 0.25, 0.125] * 2, inf)),
				\numharm, 31,
				\pitchDev, 10,
				\out, [~convolve_B]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pmono(\pulsar_mono),
				\triggerRate, 120/60/8,
				\fluxMF, 0.3,
				\fluxMD, ~pmodenv.(Pseq([30, 15],inf), Pseq([6],inf)),
				\grainFreq, 50,
				\overlap, 1000,
				\pmRatio, 100,
				\pmIndex, 3,
				\density, 0.4,
				\polarityMod, 0,
				\out, [~convolve_B]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\kick,
					Pbind(
						\freq, 40,
						\amp, 3,
						\dec, 1.5,
						\fb, ~pmodenv.(Pseq([0.5, 1.2, 0.5],inf), Pkey(\dur)),
						\index, 0.5,
					)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[1])
					<> Pdef(\p1)
					<> Pbind(\instrument, \fmKick)
				),
				\out, [~mainout, ~convolve_A]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq3,
					Pdef(\fb1Mod)
					<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4, 7], mod: 7)
					<> Pdef(\p1)
					<> Pbind(\instrument, \fb1)
				),
				\out, [~mainout, ~convolve_B, ~miverb]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq1,
					Pdef(\fb2Mod)
					<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4], mod: 5)
					<> Pdef(\p1)
					<> Pbind(\instrument, \fb2)
				),
				\out, [~mainout]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq2,
					Pbindf(Pdef(\fmPercMod), \freq, 15.midicps)
					// <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4], mod: 3)
					<> Pdef(\p1)
					<> Pbind(\instrument, \fmPerc)
					),
				\out, [~convolve_A]
		).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, -18,
				\atk, 500,
				\rel, 100,
				\swap, 0,
				// \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([4], inf)),
				\out, [~mainout, ~miVerb]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);


		////////////////////////////////////////////
		\e.postln;
		sectionLength = 64;

		sp.par(
			Pmono(\grainSlicer_mono,
				\amp, 1,
				\buf, a.at(\file),
				//lowest to highest
				\overlap, 100,
				// \trigRate, 100,
				\trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
				\slice, ~pGetSlice.(5, a),
				// \slice, 10,
				\gain, -12,
				\posRate, 0.1,
				\rate, 0.5,
				// \rate, 2,
				\out, [ ~mainout, ~convolve_B]
			).finDur(64+64+64+2)
		);

/*		sp.par(
			Pbindf(
				Pdef(\filter),
				\blend, ~pmodenv.(Pseq([0, 1],inf), Pseq([6], inf)),
				\freq, ~pmodenv.(Pseq([300, 800],inf), Pseq([4], inf)),
				\res, 0.5,
				\out, [~mainout]
			)
		);*/

		sp.par(
				Pmono(\fftStretch_magAbove_mono,
					\amp, 1,
					\gain, 0,
					\buf, ~specBuff2.at(\file),
					\analysis, [~specBuff2.at(\analysis)],
					\fftSize, ~specBuff2.at(\fftSize),
					\rate, 0.2,
					\pos, 0.1,
					\len, 0.4,
					\filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
				\out, [~convolve_A, ~mainout]
				).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pmono(\driftingSines_mono),
				\amp, 1,
				\gain, 0,
				\freq, 400,
				// \freq, ~pmodenv.(Pseq([150, 400],inf), Pxrand([0.5, 0.25, 0.125] * 2, inf)),
				\numharm, 31,
				\pitchDev, 100,
				\out, [~convolve_B]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, -18,
				\atk, 500,
				\rel, 100,
				\swap, 0,
				\swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([4], inf)),
				\out, [~mainout, ~miVerb]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);
		// sp.wait(2);
		////////////////////////////////////////////
		\f.postln;
		sectionLength = 64;

		Pdef(\p1,
				~makeSubdivision.(
					PlaceAll([1, Rest(1), 1, 1], inf),
					PlaceAll([4, 4, 4, 4], inf)
			)
		);

		sp.par(
			Pbindf(
				Pmono(\driftingSines_mono),
				\tempo, 130/60,
				\amp, 0.1,
				\gain, -6,
				\freq, 50,
				// \freq, ~pmodenv.(Pseq([150, 400],inf), Pxrand([0.5, 0.25, 0.125] * 2, inf)),
				\numharm, 31,
				\pitchDev, 400,
				\out, [~convolve_B]
			).finDur(sectionLength)
		);

		Pdef(\fb2Mod,
			Pbind(
				\amp, 0.05,
				\buf, b,
				\window, 512,
				\dec, Pkey(\dur),
				\sustainTime, Pkey(\dur),
				// \exciter, 0.6,
				// \density, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
				\exciter, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
				\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pseq([4, 8, 2, 4],inf)),

				\time, ~pmodenv.(Pseq([0.005, 0.001, 0.010],inf) * 1 * Pkey(\groupdelta), Pkey(\dec)),
				\damp, ~pmodenv.(Pseq([1, 0],inf), Pwhite(0.01, 0.4, inf)),
				// \damp, ~pmodenv.(Pkey(\cycledelta) * 1, Pkey(\dec)),

				\filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),

				\delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5, 0.25], inf)),

				\impulse, ~pmodenv.(Pwhite(5600, 20000, inf), Pkey(\dec)),
				\rev, ~pmodenv.(Pexprand(0.1, 10, inf), Pseq([2, 4], inf)),

				\pan, ~pmodenv.(Pwhite(-0.8, 0.8, inf), Pseq([0.5, 0.25],inf)),
			)
		);

		sp.par(
			Pbindf(
				Pdef(\kick,
					Pbind(
						\freq, 30,
						\amp, 1,
						\gain, -12,
						\dec, 0.5,
						\fb, ~pmodenv.(Pseq([0.5, 1.2, 0.5],inf), Pkey(\dur)),
						\index, 1,
					)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 2], mod: 3)
					<> Pdef(\p1)
					<> Pbind(\instrument, \fmKick)
				),
				\out, [~mainout]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq3,
					Pbind(
						\amp, 0.7,
						// \dur, Pwhite(0.125, 0.5, inf).stutter(4),
						\dec, Pkey(\dur) * 1,
						\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
						\time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 1),inf), Pkey(\dec)),
						\damp, ~pmodenv.(Pseq([0.1, 1],inf), Pkey(\dec)),
						// \exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec), 1, Pseq([\sine],inf)),
						\impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pkey(\dec)),
						\spont, ~pmodenv.(Pseq([60,1000],inf), Pkey(\dec)),
						\boost,  ~pmodenv.(Pseq([20000,200],inf), Pkey(\dec)),
						\restore, 40,
						\dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
						\rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
						\pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
						\gain, 0,
					)
					<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4], mod: 6)
					<> Pdef(\p1)
					<> Pbind(\instrument, \fb1)
				),
				\out, [~mainout, ~convolve_B, ~miverb]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq1,
					Pdef(\fb2Mod)
					// <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4], mod: 5)
					<> Pdef(\p1)
					<> Pbind(\instrument, \fb2)
				),
				\out, [~mainout, ~convolve_A]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, 0,
				\atk, 100,
				\rel, 500,
				\swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([4], inf)),
				// \swap, 0,
				\out, [~mainout, ~miVerb]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);
		////////////////////////////////////////////

		\g.postln;
		sectionLength = 74;

		Pdef(\p1,
				~makeSubdivision.(
					PlaceAll([1, 1, 1.5, 1.5], inf),
					PlaceAll([5, 5, 4, 4], inf)
			)
		);

		sp.par(
			Pbindf(
				Pmono(\pulsar_mono),
				\triggerRate, 120/60/8,
				\fluxMF, 0.3,
				\fluxMD, ~pmodenv.(Pseq([30, 15],inf), Pseq([6],inf)),
				\grainFreq, 50,
				\overlap, 1000,
				\pmRatio, 100,
				\pmIndex, 3,
				\density, 0.4,
				\polarityMod, 0,
				\out, [~mainout]
			).finDur(sectionLength)
		);

		Pdef(\fb2Mod,
			Pbind(
				\amp, 0.05,
				\buf, b,
				\window, 512,
				\dec, Pkey(\dur),
				\sustainTime, Pkey(\dur),
				// \exciter, 0.6,
				// \density, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
				\exciter, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
				\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pseq([4, 8, 2, 4],inf)),

				\time, ~pmodenv.(Pseq([0.005, 0.001, 0.010],inf) * 1 * Pkey(\groupdelta), Pkey(\dec)),
				\damp, ~pmodenv.(Pseq([1, 0],inf), Pwhite(0.01, 0.4, inf)),
				// \damp, ~pmodenv.(Pkey(\cycledelta) * 1, Pkey(\dec)),

				\filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),

				\delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5, 0.25], inf)),

				\impulse, ~pmodenv.(Pwhite(5600, 20000, inf), Pkey(\dec)),
				\rev, ~pmodenv.(Pexprand(0.1, 10, inf), Pseq([2, 4], inf)),

				\pan, ~pmodenv.(Pwhite(-0.8, 0.8, inf), Pseq([0.5, 0.25],inf)),
			)
		);

		sp.par(
			Pbindf(
				Pdef(\kick,
					Pbind(
						\freq, 30,
						\amp, 3,
						\gain, -12,
						\dec, 0.5,
						\fb, ~pmodenv.(Pseq([0.5, 1.2, 0.5],inf), Pkey(\dur)),
						\index, 1,
					)
					<> ~filterBeat.(key: Pkey(\eventcount), beat:[4])
					<> Pdef(\p1)
					<> Pbind(\instrument, \fmKick)
				),
				\out, [~mainout]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\seq3,
					Pbind(
						\amp, 0.7,
						// \dur, Pwhite(0.125, 0.5, inf).stutter(4),
						\dec, Pkey(\dur) * 1,
						\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
						\time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 1),inf), Pkey(\dec)),
						\damp, ~pmodenv.(Pseq([0.1, 1],inf), Pkey(\dec)),
						// \exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec), 1, Pseq([\sine],inf)),
						\impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pkey(\dec)),
						\spont, ~pmodenv.(Pseq([60,1000],inf), Pkey(\dec)),
						\boost,  ~pmodenv.(Pseq([20000,200],inf), Pkey(\dec)),
						\restore, 40,
						\dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
						\rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
						\pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
						\gain, 0,
					)
					<> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4], mod: 6)
					<> Pdef(\p1)
					<> Pbind(\instrument, \fb1)
				),
				\out, [~mainout, ~convolve_B, ~miverb]
			).finDur(sectionLength)
		);


		sp.par(
			Pbindf(
				Pdef(\seq1,
					Pdef(\fb2Mod)
					// <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4], mod: 5)
					<> Pdef(\p1)
					<> Pbind(\instrument, \fb2)
				),
				\out, [~mainout, ~convolve_A]
			).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, 0,
				\atk, 100,
				\rel, 500,
				\swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([4], inf)),
				\swap, 0,
				\out, [~mainout, ~miVerb]
			).finDur(sectionLength)
		);

		sp.wait(sectionLength);

		////////////////////////////////////////////
		\g.postln;
		sectionLength = 128;

		sp.par(
			Pbindf(
				Pmono(\convolved_pulsar_mono),
				\buf, d,
				\window, 4096,
				\triggerRate, 14,
				\fluxMF, 1.5,
				\fluxMD, 1,
				\grainFreq, 41,
				\overlap, 0.1,
				\pmRatio, 0.24,
				\pmIndex, 100,
				\density, 1,
				\polarityMod, 1,
				\gain, -18,
				\out, [~convolve_A, ~convolve_B]
			).finDur(sectionLength)
		);

		sp.par(
			Pmono(\grainSlicer_mono,
				\amp, 1,
				\buf, a.at(\file),
				\overlap, 100,
				\trigRate, Pseg(Pseq([10, 1000,], inf), 4, 'exp' , inf),
				\slice, ~pGetSlice.(51, a),
				\gain, -6,
				\posRate, 0.1,
				\rate, 0.25,
				\out, [~convolve_B]
			).finDur(sectionLength)
		);

		sp.par(
				Pmono(\fftStretch_magAbove_mono,
					\amp, 1,
					\gain, 0,
					\buf, ~specBuff2.at(\file),
					\analysis, [~specBuff2.at(\analysis)],
					\fftSize, ~specBuff2.at(\fftSize),
					\rate, 1,
					\pos, 0.7,
					\len, 0.4,
					\filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
				\out, [ ~mainout, ~miverb]
				).finDur(sectionLength)
		);

		sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, 0,
				\atk, 100,
				\rel, 500,
				\swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([4], inf)),
				\swap, 0,
				\out, [~mainout, ~miVerb]
			).finDur(sectionLength)
		);


	})
).play(t);
)