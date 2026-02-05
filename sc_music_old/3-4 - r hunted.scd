Pdef.clear;

(
~mainout = 0;
~longverb = Bus.audio(s,2);
~longverb2 = Bus.audio(s,2);
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

~bufA = Buffer.alloc(s, s.sampleRate * 0.1);
~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
)

(
//fx routing
Pdef(\modal, Pmono(\padKlank, \inbus, ~modal, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\formant, Pmono(\formantBank, \inbus, ~formant, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\grain, Pmono(\liveGrain_mono, \inbus, ~grain, \bufnum, ~bufA, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\tape, Pmono(\tape, \inbus, ~tape, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\pitchShift, Pmono(\pitchShift, \inbus, ~pitchShift, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
Pdef(\filter, Pmono(\vasem12, \inbus, ~filter, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
)

(
Pspawner({| sp |
	////////////////////////////////////////////////////////
	\a.postln;
	sp.par(
		~appendRest.(
			Pbindf(
				Pdef(\perc1),
				\out, [~formant, ~grain, ~modal]),
			6, 4
		).finDur(64)
	);

	//FX
	sp.par(
		PfadeIn(
			Pbindf(
				Pdef(\modal),
				\impulse, 10000,
				\lpf, 6000,
				\dev, 0.01,
				\stretch, 0.25,
				\atk, 0.5,
				\dec, 20,
				\decayscale, 1,
				\detune, 100,
				\gain, -20,
				\out, [~formant, ~grain]
			),
			32).finDur(64)
	);

	sp.par(
		PfadeIn(
			Pbindf(
				Pdef(\grain),
				\bufnum, ~bufA,
				\overlap, 80,
				\trigRate, 1000,
				\posRate, 0.12,
				\rate, 0.99,
				\recRate, 1,
				\polarityMod, 1,
				// \overdub, 0.4,
				\feedback, 0.125,
				\gain, -18,
				\out, [~formant]
			), 16
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\formant),
			\dur, 2,
			\drywet, ~pmodenv.(Pwhite(0, 0.8), Pkey(\dur), 1, \sin),
			\out, [~mainout]
		).finDur(64)
	);

	sp.wait(64);
	////////////////////////////////////////////////////////

	\b.postln;
	sp.par(
		~appendRest.(
			Pbindf(
				Pdef(\perc1),
				\out, [~formant, ~grain, ~modal]),
			6, 3
		).finDur(96)
	);

	sp.par(
		PfadeIn(
			Pbindf(
				Pdef(\fb1Patt3),
				\gain, -8,
				\out, [~formant, ~modal]
			), 64
		).finDur(96)
	);

	//FX
	sp.par(
		Pbindf(
			Pdef(\modal),
			\impulse, 10000,
			\lpf, 6000,
			\dev, 0.01,
			\stretch, 0.25,
			\atk, 0.5,
			\dec, 20,
			\decayscale, 1,
			\detune, 100,
			\gain, -20,
			\out, [~formant, ~grain]
		).finDur(96)
	);

	sp.par(
		Pbindf(
			Pdef(\formant),
			\dur, 2,
			\drywet, ~pmodenv.(Pwhite(0, 0.9), Pkey(\dur), 1, \sin),
			\out, [~mainout, ~grain]
		).finDur(96)
	);

	sp.par(
		Pbindf(
			Pdef(\grain),
			\bufnum, ~bufB,
			\overlap, 20,
			\trigRate, 100,
			\posRate, 0.5,
			\rate, 0.5,
			\recRate, 0.5,
			\polarityMod, 0.5,
			\overdub, 0.5,
			// \feedback, 0.125,
			\feedback, 0,
			\gain, -12,
			\out, [~mainout]
		).finDur(96)
	);

	sp.wait(96);
	////////////////////////////////////////////////////////

	\c.postln;
	sp.par(
		~pFade.(
			Pbindf(
				Pdef(\perc1),
				\out, [~formant, ~grain, ~modal]
			),

			Pbindf(
				Pdef(\perc2a),
				\rel, ~pmodenv.(Pwhite(0.4, 4), Pkey(\dur), 1, \sin),
				\out, [~filter, ~modal]
			),
			//fadetime
			64
		).finDur(64)
	);

	sp.par(
		PfadeOut(
			Pbindf(
				Pdef(\fb1Patt3),
				\gain, -8,
				\out, [~formant, ~modal]
			),
			32
		).finDur(32)
	);

	sp.par(
		PfadeIn(
			Pmono(\driftingSines_mono, \amp, 1, \freq, 202, \pitchDev, 7, \numHarm, 2, \out, [~formant])
			, 16
		).finDur(64)
	);


	//FX
	sp.par(
		Pbindf(
			Pdef(\filter),
			\blend, Pseg(Pseq([0, 1],inf), 2, 'lin' , inf),
			\freq, Pseg(Pseq([300, 800],inf), 4, 'lin' , inf),
			\res, 0.5,
			\out, [~formant, ~modal, ~mod1_in]
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\formant),
			\dur, 2,
			\drywet, ~pmodenv.(Pwhite(0, 0.25), Pkey(\dur), 1, \sin),
			// \drywet, 0,
			\out, [~mainout, ~grain, ~mod1_in]
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\grain),
			\bufnum, ~bufB,
			\overlap, 20,
			\trigRate, 100,
			\posRate, 0.5,
			\rate, 0.5,
			\recRate, 0.5,
			\polarityMod, 0.5,
			\overdub, 0.5,
			\feedback, 0.125,
			\gain, -12,
			\out, [~mainout]
		).finDur(64)
	);

	sp.wait(64);
	////////////////////////////////////////////////////////
	\d.postln;

	sp.par(
		~appendRest.(
			Pbindf(
				Pdef(\perc2a),
				\rel, ~pmodenv.(Pwhite(0.4, 4), Pkey(\dur), 1, \sin),
				\out, [~filter]
			), 8, 2
		).finDur(64)
	);

	sp.par(
		~pFade.(
			Pmono(\driftingSines_mono, \amp, 1, \freq, 202, \pitchDev, 7, \numHarm, 2, \out, [~formant]),
			Pmono(\driftingSines_mono, \amp, 1, \freq, 101, \pitchDev, 7.5, \numHarm, 4, \out, [~formant]),
			32
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\filter),
			\blend, Pseg(Pseq([0, 1],inf), 2, 'lin' , inf),
			\freq, Pseg(Pseq([300, 800],inf), 4, 'lin' , inf),
			\res, 0.5,
			\out, [~formant, ~mod1_in, ~grain]
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\fb1Patt3),
			\gain, -8,
			\out, [~formant]
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\formant),
			\dur, 2,
			\drywet, ~pmodenv.(Pwhite(0, 0.3), Pkey(\dur), 1, \sin),
			\out, [~mainout]
		).finDur(64)
	);

	sp.par(
		Pdef(\follower1,
			Pmono(\envfollow,
				\dur, 0.1,
				\atk, 0.01,
				\rel, 0.1,
				\amp, 0.1,
				\mul, 0.1,
				\add, 0.01,
				\inbus, ~mod1_in,
				\out, ~mod1_out,
			)
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\grain),
			\bufnum, ~bufA,
			\overlap, 100,
			\trigRate, 100,
			\posRate, ~mod1_out,
			\rate, 2,
			\recRate, 1,
			\polarityMod, 1,
			\overdub, 0,
			\feedback, 0,
			\gain, -6,
			\out, [~mainout]
		).finDur(64)
	);

	sp.wait(64);
	////////////////////////////////////////////////////////
	\e.postln;

	sp.par(
		~pFade.(
			Pbindf(
				Pdef(\perc2a),
				\rel, ~pmodenv.(Pwhite(0.4, 4), Pkey(\dur), 1, \sin),
				\out, [~filter]
			),
			Pbindf(
				Pdef(\perc2b),
				// \gain, -16,
				\rel, ~pmodenv.(Pwhite(0.4, 2), Pkey(\dur), 1, \sin),
				\out, [~filter, ~grain]
			),
			16
		).finDur(64)
	);


	sp.par(
		Pmono(\driftingSines_mono, \amp, 1, \freq, 101, \pitchDev, 7.5, \numHarm, 4, \out, [~filter])
	);

	sp.par(
		Pbindf(
			Pdef(\fb1Patt3),
			\gain, -8,
			// \restore, 34,
			\out, [~filter]
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\filter),
			\blend, Pseg(Pseq([0, 1],inf), 4, 'lin' , inf),
			\freq, Pseg(Pseq([80, 800],inf), 4, 'lin' , inf),
			\res, 0.5,
			\out, [~mainout]
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\grain),
			\bufnum, ~bufB,
			\overlap, 100,
			\trigRate, 1000,
			\posRate, 1,
			\rate, 2,
			\recRate, 1,
			\polarityMod, 1,
			\overdub, 0,
			\feedback, 0,
			\gain, -6,
			\out, [~mainout]
		).finDur(64)
	);

	sp.wait(64);
	////////////////////////////////////////////////////////
	\f.postln;
	sp.par(
		Pbindf(
			Pdef(\perc2c),
			// \gain, -16,
			\rel, ~pmodenv.(Pwhite(0.4, 2), Pkey(\dur), 1, \sin),
			\out, [~mainout, ~grain, ~modal]
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\fb1Patt3),
			\gain, -4,
			\restore, Pseg(Pseq([34, 40], inf) , 8, 'lin', inf),
			\out, [~mainout, ~grain]
		)
	);

	sp.par(
		Pbindf(
			Pdef(\grain),
			\bufnum, ~bufB,
			\overlap, 40,
			\trigRate, 100,
			\posRate, 1,
			\rate, 1,
			\recRate, 1,
			\polarityMod, 1,
			\overdub, 0,
			\feedback, 0.3,
			\gain, -12,
			\out, [~mainout]
		).finDur(64)
	);

	sp.wait(64);
	////////////////////////////////////////////////////////
	\g.postln;

	sp.par(
		Pbindf(
			Pdef(\perc3),
			// \gain, -16,
			\rel, ~pmodenv.(Pwhite(0.4, 1), Pkey(\dur), 1, \sin),
			\out, [~mainout, ~grain, ~modal]
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\fb1Patt4),
			\gain, -8,
			\restore, Pseg(Pseq([34, 40], inf) , 8, 'lin', inf),
			\out, [~modal, ~mainout]
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\modal),
			\impulse, 10000,
			\lpf, 6000,
			\dev, 1,
			\stretch, 0.01,
			\atk, 0.5,
			\dec, 1,
			\decayscale, 1,
			\detune, 0,
			\gain, -18,
			\out, [~mainout, ~grain]
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\grain),
			\bufnum, ~bufB,
			\overlap, 40,
			\trigRate, 100,
			\posRate, 1,
			\rate, 1,
			\recRate, 1,
			\polarityMod, 1,
			\overdub, 0,
			\feedback, 0.3,
			\gain, -12,
			\out, [~mainout]
		).finDur(64)
	);

	sp.wait(64);
	////////////////////////////////////////////////////////
	\h.postln;

	sp.par(
		Pbindf(
			Pdef(\perc3),
			// \gain, -16,
			\rel, ~pmodenv.(Pwhite(0.4, 2), Pkey(\dur), 1, \sin),
			\out, [~formant, ~grain]
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\fb1Patt5),
			\gain, -8,
			\out, [~mainout]
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\grain),
			\bufnum, ~bufB,
			\overlap, 40,
			\trigRate, 100,
			\posRate, 1,
			\rate, 0.99,
			\recRate, 1,
			\polarityMod, 1,
			\overdub, 0,
			\feedback, 0.3,
			\gain, -6,
			\out, [~formant]
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\formant),
			\dur, 2,
			\drywet, ~pmodenv.(Pwhite(0, 0.9), Pkey(\dur), 1, \sin),
			\out, [~mainout]
		).finDur(64)
	);

	sp.wait(64);
	////////////////////////////////////////////////////////
	\i.postln;

	sp.par(
		Pbindf(
			Pdef(\perc3),
			\freq, 12.midicps,
			// \gain, -16,
			\rel, ~pmodenv.(Pwhite(0.4, 2), Pkey(\dur), 1, \sin),
			\out, [~formant, ~mainout]
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\fb1Patt4),
			\gain, -16,
			\out, [~mainout, ~grain]
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\formant),
			\dur, 2,
			\drywet, ~pmodenv.(Pwhite(0, 0.9), Pkey(\dur), 1, \sin),
			\out, [~grain]
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\grain),
			\startsamp, 0,
			\endsamp, 1,
			\pos, 0,
			\overlap, 40,
			\trigRate, 100,
			\posRate, 1,
			\rate, 1,
			\recRate, 1,
			\polarityMod, 1,
			\overdub, 0.4,
			\feedback, 0.3,
			\gain, -18,
			\out, [~mainout]
		).finDur(64)
	);

	sp.wait(64);
	////////////////////////////////////////////////////////
	\j.postln;

	sp.par(
		Pbindf(
			Pdef(\fb1Patt1),
			\gain, -8,
			\out, [~grain]
		).finDur(64)
	);

	sp.par(
		Pbindf(
			Pdef(\grain),
			\startsamp, 0,
			\endsamp, 1,
			\pos, 0,
			\overlap, 10,
			\trigRate, 100,
			\posRate, 0.5,
			\rate, 0.5,
			\recRate, 1,
			\polarityMod, 1,
			\overdub, 0,
			\feedback, 0,
			\gain, -6,
			\out, [~mainout]
		).finDur(64)
	);

	sp.wait(64);
	sp.suspendAll();
}).play(t);
)

(
Pdef(\perc1,
	Pbind(
		\instrument, \fmPerc,
		\amp, 1,
		\freq, 23.midicps,
		\dec, Pkey(\dur) * 1,
		\atk, ~pmodenv.(Pwhite(0.01, 0.05, inf), Pkey(\dec)),
		\rel, 0.5,
		\feedback, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
		\fbmod, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),

		\carRatio, 2,
		\m1Ratio, 1,
		\m2Ratio, 7,
		\m3Ratio, 9,
		//
		\i1atk, ~pmodenv.(Pwhite(0.5, 0.1), Pkey(\dec)),
		\i2atk, ~pmodenv.(Pwhite(0.8, 0.01), Pkey(\dec)),
		\i1rel, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\i2rel, ~pmodenv.(Pwhite(0, 0.5), Pkey(\dec)),

		\index1, ~pmodenv.(Pkey(\groupdelta) + 1e4, Pkey(\dec)),
		\index2, ~pmodenv.(1 - Pkey(\groupdelta) + 1e4, Pkey(\dec)),
		\iscale, ~pmodenv.((1 - Pkey(\cycledelta)) + 1e-4, Pkey(\dec)),
		\spread,~pmodenv.(Pwhite(10, 30, inf), Pkey(\dec)),
		\mix, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		// \mix, 1,
		//\drive, 0,
		\drive, ~pmodenv.(Pwhite(0, 16), Pkey(\dec)),
		\gain, -6,
	) <> ~makeSubdivision.(
		Pseq([1.5, 1.5, 1, 1], 1),
		Pseq(#[4, 2, 6], 1)
	)
);
//brutal
Pdef(\perc2a,
	Pbind(
		\amp, 1,
		\freq, 12.midicps,
		\dec, Pkey(\dur) * 1,
		\rel, 1,

		\carRatio, 2,
		\m1Ratio, 0.5,
		\m2Ratio, 1,
		\m3Ratio, 2,

		// \index1, ~pmodenv.(Pkey(\cycledelta) * 0, Pkey(\dec)),
		\index2, ~pmodenv.(1 - Pkey(\cycledelta) * 0.125 + 1e4, Pkey(\dec)),
		\iscale, ~pmodenv.((1 - Pkey(\groupdelta)) * 0.125 + 1e-4, Pkey(\dec)),
		\spread,~pmodenv.(Pwhite(1, 3, inf), Pkey(\dec)),
		\mix, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\drive, ~pmodenv.(Pwhite(0, 16), Pkey(\dec)),
		\gain, -12,
	)
	// <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
	<> Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 2, 0.5))
	// <> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 5])
	<> ~makeSubdivision.(
		Pseq([1.5, 1.5, 1, 1], 1),
		Pseq(#[4, 2, 6], 1)
	)
	<> Pbind(\instrument, \fmPerc)
);

//brutal
Pdef(\perc2b,
	Pbind(
		\amp, 1,
		\freq, 14.midicps,
		\dec, Pkey(\dur) * 1,
		\rel, 1,

		\carRatio, 2,
		\m1Ratio, 0.5,
		\m2Ratio, 2,
		\m3Ratio, 1,
		// \index1, ~pmodenv.(Pkey(\cycledelta) - 1e4, Pkey(\dec)),
		\index2, ~pmodenv.(1 - Pkey(\groupdelta) * 0.125 + 1e4, Pkey(\dec)),
		\iscale, ~pmodenv.((1 - Pkey(\cycledelta)) * 0.125 + 1e-4, Pkey(\dec)),
		\spread,~pmodenv.(Pwhite(10, 30, inf), Pkey(\dec)),
		\mix, ~pmodenv.(Pwhite(0.25, 1), Pkey(\dec)),
		\drive, ~pmodenv.(Pwhite(0, 16), Pkey(\dec)),
		\gain, -12,
	)
	<> Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 2, 0.5))
	// <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
	<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 5])
	<> ~makeSubdivision.(
		Pseq([1.5, 1.5, 1, 1], 1),
		Pseq(#[4, 2, 6], 1)
	)
	<> Pbind(\instrument, \fmPerc)
);

//brutal
Pdef(\perc2c,
	Pbind(
		\amp, 1,
		\freq, 14.midicps,
		\dec, Pkey(\dur) * 1,
		\rel, 1,
		\carRatio, 2,
		\m1Ratio, 0.5,
		\m2Ratio, 2,
		\m3Ratio, 1,
		// \index1, ~pmodenv.(Pkey(\cycledelta) - 1e4, Pkey(\dec)),
		\index2, ~pmodenv.(1 - Pkey(\groupdelta) * 0.125 + 1e4, Pkey(\dec)),
		\iscale, ~pmodenv.((1 - Pkey(\cycledelta)) * 0.125 + 1e-4, Pkey(\dec)),
		\spread,~pmodenv.(Pwhite(10, 30, inf), Pkey(\dec)),
		\mix, ~pmodenv.(Pwhite(0.5, 1), Pkey(\dec)),
		\drive, ~pmodenv.(Pwhite(0, 16), Pkey(\dec)),
		\gain, -12,
	)
	// <> Pbind(\stretch, Pkey(\groupdelta).linlin(0, 1, 2, 0.5))
	// <> Pbind(\timingOffset, Pkey(\groupcount) / Pkey(\groupdelta))
	<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1, 3, 5])
	<> ~makeSubdivision.(
		Pseq([1.5, 1.5, 1, 1], 1),
		Pseq(#[4, 2, 6], 1)
	)
	<> Pbind(\instrument, \fmPerc)
);


//brutal2
Pdef(\perc3,
	Pbind(
		\amp, 1,
		\freq, 12.midicps,
		\dec, Pkey(\dur) * 1,
		\rel, 2,
		\carRatio, 2,
		\m1Ratio, 0.5,
		\m2Ratio, 1,
		\m3Ratio, 2,
		// \index1, ~pmodenv.(Pkey(\cycledelta) - 1e4, Pkey(\dec)),
		\index2, ~pmodenv.(1 - Pkey(\groupdelta) * 0.125 + 1e4, Pkey(\dec)),
		\iscale, ~pmodenv.((1 - Pkey(\cycledelta)) * 0.125 + 1e-4, Pkey(\dec)),
		\spread,~pmodenv.(Pwhite(10, 30, inf), Pkey(\dec)),
		\mix, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\drive, ~pmodenv.(Pwhite(0, 16), Pkey(\dec)),
		\gain, -12,
	)
	<> ~makeSubdivision.(
		Pseq([1.5, 1.5, 1, 1], 1),
		Pseq(#[4, 2, 6], 1)
	)
	<> Pbind(\instrument, \fmPerc)
);

//brutal3
Pdef(\perc4,
	// Pmono(\fmPerc_mono) <>
	Pbind(
		\amp, 1,
		\freq, 12.midicps,
		\dec, Pkey(\dur) * 1,
		\rel, 2,
		\feedback, ~pmodenv.(Pwhite(0.01, 0,inf), Pkey(\dec)),
		\fbmod, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),

		\carRatio, 2,
		\m1Ratio, 0.5,
		\m2Ratio, 1,
		\m3Ratio, 2,

		\i1atk, ~pmodenv.(Pwhite(0.5, 0.1), Pkey(\dec)),
		\i2atk, ~pmodenv.(Pwhite(0.8, 0.01), Pkey(\dec)),
		\i1rel, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\i2rel, ~pmodenv.(Pwhite(0, 0.5), Pkey(\dec)),
		// \index1, ~pmodenv.(Pkey(\cycledelta) - 1e4, Pkey(\dec)),
		// \index2, ~pmodenv.(1 - Pkey(\groupdelta) * 0.125 + 1e4, Pkey(\dec)),
		\iscale, ~pmodenv.((1 - Pkey(\cycledelta)) * 0.125 + 1e-4, Pkey(\dec)),
		\spread,~pmodenv.(Pwhite(10, 30, inf), Pkey(\dec)),
		\mix, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\drive, ~pmodenv.(Pwhite(0, 16), Pkey(\dec)),
		\gain, -12,
	)
	<> ~makeSubdivision.(
		Pseq([1, 1, 2, 1.5], 1),
		Pseq(#[4, 2, 6], 1)
	)
	<> Pbind(\instrument, \fmPerc)
);

Pdef(\fb1Patt1, Pmono(\fb1_mono) <> Pbind(
	\amp, 1,
	\dur, Pwhite(0.125, 0.5, inf).stutter(4),
	\dec, Pkey(\dur) * 2,
	\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
	\time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 100),inf), Pkey(\dec)),
	\res, ~pmodenv.(Pseq([1, 0],inf), Pseq([0.25, 0.25], inf)),
	\damp, ~pmodenv.(Pseq([0.1, 0.5],inf), Pkey(\dec)),
	\filter, ~pmodenv.(Pseq([30, 100],inf), Pkey(\dec)),
	\exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec), 1, Pseq([\sine],inf)),
	\dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
	\rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
	\pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
	\gain, -6,
	)
);

Pdef(\fb1Patt2, Pbind(
	\amp, 1,
	\dec, Pkey(\dur) * 2,
	\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
	\time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 100),inf), Pkey(\dec)),
	\res, ~pmodenv.(Pseq([1, 0],inf), Pseq([0.25, 0.25], inf)),
	\damp, ~pmodenv.(Pseq([0.1, 1],inf), Pkey(\dec)),
	\dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
	\pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
	\gain, -6,
	) <> ~makeSubdivision.(
		Pseq([3, 1], 1),
		Pseq([4, 5, 3, 6, 2], 1)
	)
	<> Pbind(\instrument, \fb1)
// <> Pmono(\fb1_mono)
);

//lowbass
Pdef(\fb1Patt3, Pmono(\fb1_mono) <> Pbind(
	\amp, 1,
	\dur, Pwhite(0.125, 0.5, inf).stutter(4),
	\dec, Pkey(\dur) * 1,
	\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
	\time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 1),inf), Pkey(\dec)),
	\damp, ~pmodenv.(Pseq([0.1, 0],inf), Pkey(\dec)),
	\exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec), 1, Pseq([\sine],inf)),
	//\exciter, 0,
	\impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pkey(\dec)),
	\spont, ~pmodenv.(Pseq([60,1000],inf), Pkey(\dec)),
	\boost,  ~pmodenv.(Pseq([20000,200],inf), Pkey(\dec)),
	\restore, 40,
	\dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
	//\dist, 64,
	\rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
	\pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
	\gain, 0,
	)
);

//lowbass2
Pdef(\fb1Patt4, Pmono(\fb1_mono) <> Pbind(
	\amp, 1,
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

//lowbass3
Pdef(\fb1Patt5, Pmono(\fb1_mono) <> Pbind(
	\amp, 1,
	\dur, Pwhite(0.125, 0.5, inf).stutter(4),
	\dec, Pkey(\dur) * 1,
	\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.8,inf), Pkey(\dec)),
	\time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 100),inf), Pkey(\dec)),
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


Pdef(\fb1Patt6,
	Pbind(
		\amp, 1,
		\dec, Pkey(\dur) * 1,
		\feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
		\time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 1),inf), Pkey(\dec)),
		\damp, ~pmodenv.(Pwhite(1, 0.5, inf).lincurve(0, 1, 0.1, 1, -4), Pkey(\dec)),
		\filter, ~pmodenv.(Pseq([60, 100],inf), Pkey(\dec)),
		\exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec)),
		\dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
		\rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
		\pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
		\gain, -6,
	)
	<> ~makeSubdivision.(
		Pseq([3, 1], 1),
		Pseq([4, 5, 3, 6, 2], 1)
	)
	<> Pmono(\fb1_mono)
);
)