// TODO
//try and use fb1 to optimize
// instruments: risers,
// filter function modulo

Pdef(\player).play(t,quant:[1,0]);
Pdef(\player).stop;

(
Pdef(\fmPad2,
		Pbindf(
			Pdef(\fmPad),
			// \carRatio, Pseq([1, 0.5, 2], inf).stutter(8),
			\carRatio, 0.5,
			\m1Ratio, 7,
			\m2Ratio, 13,
			\m3Ratio, 1.5,
			\mix, 1,
			\gain, 10,
		)
);
)

(
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
)

(
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
)

(
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

)


(
Pdef(\player,
	Ppar([
		Pdef(\fmPad4),
		Pdef(\shortverb)
		])
	)
)

(
Pdef(\player,
	Ppar([
		Pbindf(Pdef(\fmPad), \freq, 30, ),
		Pbindf(
			Pdef(\clapPattern),
			\out, [~mainout, ~longverb]
		),
		Pdef(\longverb),
		])
	)
)

(
Pdef(\player,
	Ppar([
		Pbindf(Pdef(\fmPad), \freq, 30),
		Pdef(\clapPattern),
		Pdef(\segPattern),
		Pdef(\subPattern),
		])
	)
)


(
Pdef(\player,
	Ppar([
		Pbindf(
			Pdef(\fmPercPatt3),
			\gain, -24,
			\out, [~convolve_A, ~convolve_B]
		),

		Pbindf(
			Pdef(\subPattern),
			\out, [~convolve_A, ~mainout]
		),

		Pbindf(
			Pdef(\clapPattern),
			\out, [~convolve_B]
		),

		Pbindf(
			Pdef(\convolve),
			\mix, 1,
			\gain, 0,
			\framesize, 4096,
			\out, [~mainout, ~longverb]
		),

		Pdef(\longverb),

		])
	)
)


(
Pdef(\player,
	Ppar([
		Pbindf(
			Pdef(\fmPad),
			\out, [~mainout]
		),

		Pbindf(
			Pdef(\fmPercPatt3),
			\gain, -24,
			\out, [~convolve_A, ~convolve_B]
		),

		Pbindf(
			Pdef(\clapPattern),
			\out, [~mainout]
		),

		Pbindf(
			Pdef(\segPattern),
			\out, [~mainout, ~delay]
		),
		Pdef(\subPattern),

		Pbindf(
			Pdef(\convolve),
			\mix, 1,
			\gain, 0,
			\framesize, 128,
			\out, [~mainout, ~longverb]
		),

		Pdef(\longverb),
		Pdef(\delay),

		])
	)
)

(
Pdef(\player,
	Ppar([
		Pbindf(
			Pdef(\percPattern5),
			\out, [~mainout, ~mod1_in, ~modDelay]
		),
		Pbindf(
			Pdef(\fmPad),

		),

/*		Pbindf(
			Pdef(\fmPercPatt3),
			\freq, Pseq([24, 23],inf).stutter(12).midicps,
			\out, [~chorus, ~delay]
		),*/

/*		Pbindf(
			Pdef(\clapPattern),
			\out, [~mainout, ~delay]
		),

		Pbindf(
			Pdef(\segPattern),
			\out, [~mainout, ~delay]
		),*/

		Pdef(\subPattern),
		// Pdef(\shortverb),
		// Pdef(\longverb),
		// Pdef(\delay),
		// Pdef(\chorus),
		Pdef(\follower1),
		Pdef(\modDelay),
		])
	)
)



Pdef(\player).play(t, quant:[1,0]);

(
Pdef(\player,
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
	)
)

(
Pdef(\player,
	Ppar([
		Pbindf(
			Pdef(\percPattern2),
			\out, [~mainout, ~longverb]
		),
		Pbindf(
			Pdef(\fmPad),
			\freq, 30
		),
		Pdef(\fmPercPatt3),
		Pbindf(
			Pdef(\clapPattern),
			\out, [~mainout, ~delay]
		),
		Pbindf(
			Pdef(\segPattern),
			\out, [~mainout, ~delay]
		),
		Pdef(\subPattern),
		Pdef(\longverb),
		Pdef(\delay),
		// Pdef(\chorus)
		])
	)
)


(
Pdef(\player,
	Ppar([
		Pbindf(
			Pdef(\percPattern2),
			\out, [~mainout, ~longverb]
		),
		Pbindf(
			Pdef(\fmPad),
			\freq, 30
		),
		Pdef(\fmPercPatt3),
		Pbindf(
			Pdef(\clapPattern),
			\out, [~mainout, ~delay]
		),
		Pbindf(
			Pdef(\segPattern),
			\out, [~mainout, ~delay]
		),
		Pdef(\subPattern),
		Pdef(\longverb),
		Pdef(\delay),
		// Pdef(\chorus)
		])
	)
)

(
Pdef(\player,
	Ppar([
		Pdef(\percPattern3),
		Pbindf(Pdef(\fmPad), \freq, 30),
		Pdef(\fmPercPatt1),
		Pdef(\clapPattern),
		Pdef(\segPattern),
		Pdef(\subPattern),
		])
	)
)

(
Pdef(\player,
	Ppar([
		// Pdef(\percPattern4),
		Pbindf(Pdef(\fmPad), \freq, 30),
		Pbindf(
			Pdef(\fmPercPatt2),
			\out, [~mainout, ~mod1_in, ~modDelay]
		),
		Pdef(\clapPattern),
		Pdef(\kickPattern1),
		Pdef(\segPattern),
		// Pdef(\subPattern),
		Pdef(\follower1),
		Pdef(\modDelay),
		])
	)
)

(
Pdef(\player,
	Ppar([
		// Pdef(\percPattern5),
		Pbindf(
			Pdef(\segPattern),
			\out, [~mainout, ~delay]
		),
		Pbindf(
			Pdef(\fmPad), \freq, 30
		),

		Pdef(\clapPattern),
		Pdef(\kickPattern1),
		Pbindf(
			Pdef(\fmPercPatt4),
			\out, [~mainout, ~mod1_in, ~modDelay]
		),

		Pbindf(
			Pdef(\hhPattern1),
			\out, [~mainout, ~mod1_in, ~modDelay]
		),

		Pdef(\subPattern),
		Pdef(\delay),
		Pdef(\follower1),
		Pbindf(
			Pdef(\modDelay),
			\lpf, 5000,
			\size, 0.012,
		)
		])
	)
)

(
Pdef(\player,
	Ppar([
		Pdef(\percPattern5),
		Pdef(\fmPad3),
		// Pdef(\subPattern),
		// Pdef(\fmPercPatt4),
		// Pdef(\clapPattern),
		// Pdef(\kickPattern1),
		// Pdef(\hhPattern1)
		])
	)
)

Pdef(\player).play(t, quant:[1,0]);
