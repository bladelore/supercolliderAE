60.cpsmidi

(
Pdef(\fmPercPatt1,
	// Pmono(\fmPerc_mono) <>
	Pbind(
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

		// \i1atk, ~pmodenv.(Pwhite(0.01, 0.1), Pkey(\dec)),
		// \i2atk, ~pmodenv.(Pwhite(0.4, 0.01), Pkey(\dec)),
		// \i1rel, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		// \i2rel, ~pmodenv.(Pwhite(0, 0.5), Pkey(\dec)),

		\index1, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
		\index2, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
		\iscale, ~pmodenv.(1 - Pkey(\cycledelta), Pkey(\dec)),
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
)
);

(
Pdef(\fmPercPatt2,
	// Pmono(\fmPerc_mono) <>
	Pbind(
		\pitchMix, 1,
		\freq, 60,
		//\dur, 0.5,
		\dec, Pkey(\dur) * 0.5,
		//\atk,0,
		// \atk, ~pmodenv.(Pkey(\groupdelta) * 0.5, Pkey(\dec)),
		// \rel, ~pmodenv.(Pwhite(0.5, 1, inf), Pkey(\dec)),
		//\rel, 0.5,
		\rel, Pkey(\dec),
		\feedback, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
		\fbmod, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
		\carRatio, 0.5,
		\m1Ratio, 2,
		\m2Ratio, 3,
		\m3Ratio, 5,
/*		\i1atk, ~pmodenv.(Pwhite(0.01, 0.1), Pkey(\dec)),
		\i2atk, ~pmodenv.(Pwhite(0.4, 0.01), Pkey(\dec)),
		\i1rel, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\i2rel, ~pmodenv.(Pwhite(0, 0.5), Pkey(\dec)),*/

/*		\mod1fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\mod2fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\mod3fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\carfb, ~pmodenv.(Pwhite(0, 0.6), Pkey(\dec)),*/

		\index1, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
		\index2, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
		\iscale, ~pmodenv.(Pkey(\cycledelta), Pkey(\dec)),
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
	<> Pbind(
		\instrument, \fmPerc,
	)
)
);

30.cpsmidi

(
Pdef(\fmPercPatt3,
	// Pmono(\fmPerc_mono) <>
	Pbind(
		\instrument, \fmPerc,
		// \freq, Pwrand([23, 24].midicps, [0.8, 0.2], inf),
		\freq, 23.midicps,
		// \freq, 30,
		\dur, Pwhite(0.5, 0.125, inf) * 2,
		//\dur, 0.5,
		\dec, Pkey(\dur) * 1,
		\atk, ~pmodenv.(Pwhite(0.01, 1, inf), Pkey(\dec)),
		//\rel, ~pmodenv.(Pwhite(0.5, 0.125, inf), Pkey(\dec)),
		\rel, 0.2,
		\feedback, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
		\fbmod, ~pmodenv.(Pwhite(0, 1,inf), Pkey(\dec)),
		\carRatio, 1,
		\m1Ratio, 1,
		\m2Ratio, 7,
		\m3Ratio, 9,

/*		\i1atk, ~pmodenv.(Pwhite(0.5, 0.1), Pkey(\dec)),
		\i2atk, ~pmodenv.(Pwhite(0.8, 0.01), Pkey(\dec)),
		\i1rel, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\i2rel, ~pmodenv.(Pwhite(0, 0.5), Pkey(\dec)),*/

		/*	\mod1fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\mod2fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\mod3fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\carfb, ~pmodenv.(Pwhite(0, 0.6), Pkey(\dec)),*/

		\index1, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
		\index2, ~pmodenv.(Pexprand(0.01, 10, inf), Pkey(\dec)),
		\iscale, ~pmodenv.(Pexprand(1, 0.1, inf), Pkey(\dec)),
		\spread,~pmodenv.(Pwhite(10, 30, inf), Pkey(\dec)),
		\mix, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		// \mix, 0,
		// \drive, 16,
		\drive, ~pmodenv.(Pwhite(0, 16), Pkey(\dec)),
		\gain, -28,
	)
)
);

(
Pdef(\fmPercPatt4,
	// Pmono(\fmPerc_mono) <>
	Pbind(
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
		/*	\mod1fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\mod2fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\mod3fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\carfb, ~pmodenv.(Pwhite(0, 0.6), Pkey(\dec)),*/
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
)
);

(
Pdef(\fmBass,
		Pbind(
		\freq, 30.midicps,
		\dec, Pkey(\dur) * 1,
		\atk, 0,
		\rel, 4,
		\feedback, 1,
		\fbmod, 0,
		\carRatio, 0,
		\m1Ratio, 1,
		\m2Ratio, 0.5,
		\m3Ratio, 1,
		\i1atk, 0.1,
		\i2atk, 0,
		\i1rel, 0.1,
		\i2rel, 0.5,
		/*	\mod1fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\mod2fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\mod3fb, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
		\carfb, ~pmodenv.(Pwhite(0, 0.6), Pkey(\dec)),*/
		\index1, 0.1,
		\index2, 0.01,
		\iscale, 0.01,
		\spread, ~pmodenv.(Pwhite(10, 30, inf), Pkey(\dec)),
		\mix, 0,
		//\mix, 0,
		//\drive, 0,
		\drive, 0,
		\gain, -14,
	)
	<> ~filterBeat.(~pattern, key: Pkey(\groupcount), beat:[1])
	<> ~filterBeat.(~pattern, key: Pkey(\eventcount), beat:[1])
	<> ~makeSubdivision.(
		Pseq(#[4, 3, 1, 2, 3, 0.75], 1),
		Pseq(#[2, 4, 4], 1)
	) <> Pbind(
		\instrument, \fmPerc,
	)
)
)
// Pdef(\fmPercPatt3).play(t, quant:[1,0]);
Pdef(\fmBass).play(t, quant:[1,0]);
Pdef(\fmPercPatt).stop;

	