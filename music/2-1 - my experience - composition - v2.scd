(
Pdef(\player).fadeTime = 24;
t.schedAbs(t.nextBar, {t.tempo_(165/60)});
Pdef(\player,
	Pseq([
		Pdef(\player1).finDur(64),
		Pdef(\player2).finDur(64),
		Pdef(\player3).finDur(64),
		Pdef(\player4).finDur(64),
		Pdef(\player5).finDur(64),
		Pdef(\player6).finDur(64),
		// Pdef(\player7).finDur(64),
		Pdef(\player8).finDur(32),
		Pdef(\player9).finDur(32),
		Pdef(\player10).finDur(32),
		Pdef(\player11).finDur(32),
		Pdef(\player12).finDur(32),
		Pdef(\player13),
		])
	).play(t);
)

(
Pdef(\player1,
	Ppar([
		Pbindf(
			Pdef(\fmPercPatt1),
			\freq, 23.midicps,
			\gain, -24,
			\out, [~convolve_A, ~convolve_B]
		),

		Pbindf(
			Pdef(\convolve),
			\mix, 1,
			\gain, -6,
			\framesize, 4096,
			\out, [~mainout, ~longverb]
		),

		Pdef(\longverb),

		])
);

Pdef(\player2,
	Ppar([
		Pbindf(
			Pdef(\percPattern2),
			\out, [ ~convolve_B]
		),

		Pbindf(
			Pdef(\fmPercPatt3),
			\freq, Pseq([24, 23],inf).stutter(12).midicps,
			\gain, -24,
			\out, [~convolve_A, ~convolve_B]
		),

		Pbindf(
			Pdef(\segPattern),
			\out, [~mainout, ~convolve_A, ~convolve_B]
		),

		Pdef(\subPattern),
		Pbindf(
			Pdef(\convolve),
			\mix, 1,
			\gain, -12,
			\framesize, 4096,
			\hpfIn, 100,
			\hpfOut, 80,
			\out, [~mainout, ~longverb]
		),

		// Pdef(\shortverb),
		Pdef(\longverb),
		// Pdef(\delay),
		// Pdef(\chorus),
		// Pdef(\follower1),
		// Pdef(\modDelay),
		])
);

Pdef(\player3,
	Ppar([
		Pbindf(
			Pdef(\percPattern2),
			\out, [ ~convolve_B]
		),

		Pbindf(
			Pdef(\fmPad4),
			\out, [~mainout, ~convolve_A, ~convolve_B, ~longverb]
		),

		Pbindf(
			Pdef(\fmPercPatt3),
			\freq, Pseq([24, 23],inf).stutter(12).midicps,
			\gain, -24,
			\out, [~convolve_A, ~convolve_B]
		),

/*		Pbindf(
			Pdef(\clapPattern),
			\out, [~convolve_A, ~convolve_B]
		),*/

		Pbindf(
			Pdef(\segPattern),
			\out, [~mainout, ~convolve_A, ~echo]
		),

		Pdef(\subPattern2),

		Pbindf(
			Pdef(\convolve),
			\mix, 1,
			\gain, -12,
			\framesize, 4096,
			\hpfIn, 100,
			\hpfOut, 80,
			\out, [~mainout, ~longverb]
		),

		// Pdef(\shortverb),
		Pdef(\longverb),
		Pdef(\delay),
		// Pdef(\chorus),
		// Pdef(\follower1),
		// Pdef(\modDelay),
		])
);

Pdef(\player4,
	Ppar([
		Pbindf(
			Pdef(\percPattern2),
			\gain, -12,
			\out, [~mainout, ~convolve_A, ~convolve_B]
		),

		Pbindf(
			Pdef(\fmPad4),
			\out, [~mainout, ~convolve_A, ~convolve_B, ~longverb]
		),

		Pbindf(
			Pdef(\fmPercPatt3),
			\freq, Pseq([24, 23],inf).stutter(12).midicps,
			\gain, -24,
			\out, [~convolve_A, ~convolve_B]
		),

/*		Pbindf(
			Pdef(\clapPattern2),
			\out, [~convolve_A, ~convolve_B, ~longverb]
		),*/

		Pbindf(
			Pdef(\segPattern2),
			\out, [~mainout, ~delay]
		),

		Pdef(\subPattern2),

		Pbindf(
			Pdef(\convolve),
			\mix, 1,
			\gain, -12,
			\framesize, 4096,
			\hpfIn, 100,
			\hpfOut, 80,
			\out, [~mainout, ~longverb]
		),

		// Pdef(\shortverb),
		Pdef(\longverb),
		Pdef(\delay),
		// Pdef(\chorus),
		// Pdef(\follower1),
		// Pdef(\modDelay),
		])
);

Pdef(\player5,
	Ppar([

		Pbindf(
			Pdef(\percPattern2),
			\gain, -12,
			\out, [~mainout, ~convolve_A, ~convolve_B]
		),

		Pbindf(
			Pdef(\fmPad2),
			\gain, 40,
			\out, [~mainout, ~convolve_A]
		),

		Pbindf(
			Pdef(\fmPercPatt3),
			\freq, Pseq([24, 23],inf).stutter(12).midicps,
			\gain, -24,
			\out, [~convolve_A, ~convolve_B]
		),

		Pbindf(
			Pdef(\clapPattern),
			\gain, -6,
			\out, [~convolve_B]
		),

		Pbindf(
			Pdef(\segPattern2),
			\out, [~mainout, ~convolve_A, ~convolve_B, ~delay]
		),

		Pdef(\subPattern3),

		Pbindf(
			Pdef(\convolve),
			\mix, 0.9,
			\gain, -6,
			\framesize, 4096,
			\out, [~mainout]
		),

		Pdef(\longverb),
		Pdef(\delay),

		])
);

Pdef(\player6,
	Ppar([
		Pbindf(
			Pdef(\percPattern5),
			\gain, -12,
			\out, [~mainout, ~convolve_A, ~convolve_B]
		),
		Pbindf(
			Pdef(\fmPad2),
			\out, [~mainout, ~convolve_A, ~convolve_B, ~longverb]
		),

		Pbindf(
			Pdef(\fmPercPatt3),
			\freq, Pseq([24, 23],inf).stutter(12).midicps,
			\gain, -24,
			\out, [~mainout]
		),

		Pbindf(
			Pdef(\clapPattern),
			\out, [~convolve_A, ~convolve_B]
		),

		Pbindf(
			Pdef(\segPattern2),
			\out, [~mainout, ~convolve_A, ~convolve_B]
		),

		Pdef(\subPattern3),

		Pbindf(
			Pdef(\convolve),
			\mix, 1,
			\gain, -12,
			\framesize, 4096,
			\hpfIn, 100,
			\hpfOut, 80,
			\out, [~mainout, ~longverb]
		),

		// Pdef(\shortverb),
		Pdef(\longverb),
		Pdef(\delay),
		// Pdef(\chorus),
		// Pdef(\follower1),
		// Pdef(\modDelay),
		])
	);

Pdef(\player7,
	Ppar([
		Pbindf(
			Pdef(\percPattern5),
			\out, [~mainout, ~mod1_in, ~modDelay]
		),
		Pbindf(
			Pdef(\fmPad4),

		),

		Pbindf(
			Pdef(\fmPercPatt1),
			\freq, Pseq([24, 23],inf).stutter(12).midicps,
			\out, [~mainout, ~delay]
		),

		Pbindf(
			Pdef(\clapPattern),
			\out, [~mainout, ~delay]
		),
		Pbindf(
			Pdef(\segPattern2),
			\out, [~mainout, ~delay]
		),

		Pdef(\subPattern3),
		// Pdef(\shortverb),
		// Pdef(\longverb),
		// Pdef(\delay),
		// Pdef(\chorus),
		Pdef(\follower1),
		Pdef(\modDelay),
		])
);

//this
Pdef(\player8,
	Ppar([
		Pbindf(
			Pdef(\percPattern3),
			\out, [~mainout]
		),
		Pbindf(
			Pdef(\fmPad2),
		),
		Pdef(\fmPercPatt2),
		Pdef(\kickPattern1),
		Pbindf(
			Pdef(\clapPattern),
			\out, [~mainout, ~delay]
		),
		Pbindf(
			Pdef(\segPattern2),
			\out, [~mainout, ~delay]
		),
		Pdef(\subPattern),
		Pdef(\longverb),
		Pdef(\delay),
		// Pdef(\chorus)
		])
);

Pdef(\player9,
	Ppar([
		Pdef(\percPattern2),
		Pbindf(
			Pdef(\fmPad4),
			\gain, 40
		),
		Pdef(\fmPercPatt1),
		Pdef(\clapPattern),
		Pdef(\segPattern2),
		Pdef(\subPattern3),
		])
);

Pdef(\player10,
	Ppar([
		Pdef(\percPattern5),
		Pbindf(
			Pdef(\segPattern),
			\out, [~mainout, ~delay]
		),
		Pbindf(
			Pdef(\fmPad4), \freq, 15
		),

		Pdef(\clapPattern),
		Pdef(\kickPattern1),
		Pbindf(
			Pdef(\fmPercPatt4),
			\out, [~mainout, ~mod1_in, ~modDelay]
		),

		Pbindf(
			Pdef(\hhPattern1),
			\out, [~mainout, ~mod1_in]
		),

		Pdef(\subPattern),
		Pdef(\delay),
		Pdef(\follower1),
		Pbindf(
			Pdef(\modDelay),
			\lpf, 5000,
			\size, 0.0095,
		)
		])
);

Pdef(\player11,
	Ppar([
		Pdef(\percPattern5),
		Pbindf(
			Pdef(\fmPad),
			\gain, -6,
			\freq, 30,
		),
		Pdef(\subPattern),
		Pdef(\fmPercPatt4),
		Pdef(\clapPattern),
		Pdef(\kickPattern1),
		Pdef(\hhPattern1)
		])
);

Pdef(\player12,
	Ppar([
		Pbindf(
			Pdef(\percPattern2),
			\out, [~mainout, ~convolve_A, ~convolve_B]
		),
		Pbindf(
			Pdef(\fmPad3),
			\gain, 10
		),
		Pdef(\subPattern3),
		Pbindf(
			Pdef(\fmPercPatt2),
			\out, [~mainout, ~convolve_A, ~convolve_B, ~longverb]
		),
		Pdef(\clapPattern),
		Pdef(\kickPattern1),
		// Pdef(\hhPattern1),
		Pbindf(
			Pdef(\convolve),
			\mix, 1,
			\gain, -12,
			\framesize, 4096,
			\hpfIn, 100,
			\hpfOut, 80,
			\out, [~mainout, ~longverb]
		),
		Pdef(\longverb),
		])
);

Pdef(\player12,
	Ppar([
		Pbindf(
			Pdef(\percPattern2),
			\out, [~mainout, ~convolve_A, ~convolve_B]
		),
		Pbindf(
			Pdef(\fmPad3),
			\gain, 10
		),
		Pdef(\subPattern3),
		Pbindf(
			Pdef(\fmPercPatt2),
			\out, [~mainout, ~convolve_A, ~convolve_B, ~longverb]
		),
		Pdef(\clapPattern),
		Pdef(\kickPattern1),
		// Pdef(\hhPattern1),
		Pbindf(
			Pdef(\convolve),
			\mix, 1,
			\gain, -12,
			\framesize, 4096,
			\hpfIn, 100,
			\hpfOut, 80,
			\out, [~mainout, ~longverb]
		),
		Pdef(\longverb),
		])
);

Pdef(\player13,
	Ppar([
		Pbindf(
			Pdef(\fmPad4),
			// \gain, 10
		),
	])
);
)


Pdef(\player13).play(t, quant:[1,0]);
