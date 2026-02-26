Safety(s).disable

(
Pdef(\player).fadeTime = 24;
t.schedAbs(t.nextBar, {t.tempo_(165/60)});
Pdef(\player,
	Pseq([
		// Pdef(\player1).finDur(64),
		Pdef(\player2).finDur(64),
		Pdef(\player3).finDur(64),
		Pdef(\player4).finDur(64),
		Pdef(\player5).finDur(64),
		Pdef(\player6).finDur(64),
		Pdef(\player7).finDur(64),
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
    ~recorders = ~recordBuses.value(
        ~busArr,
        Platform.recordingsDir +/+ "my experience6/%.wav"
    );
)

(
    ~busArr = [
        ~convolution_out=Bus.audio(s,2),
        ~longverb_out=Bus.audio(s,2),
        ~segPattern_out=Bus.audio(s,2),
        ~sub_out=Bus.audio(s,2),
        ~fmPad_out=Bus.audio(s,2),
        ~delay_out=Bus.audio(s,2),
        ~percPattern_out=Bus.audio(s,2),
        ~fmPerc_out=Bus.audio(s,2),
        ~clap_out=Bus.audio(s,2),
        ~modDelay_out=Bus.audio(s,2),
        ~kick_out=Bus.audio(s,2),
        ~hh_out=Bus.audio(s,2),
    ]
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
			\out, [~mainout, ~longverb, ~convolution_out]
		),

		Pbind(\out, [~mainout, ~longverb_out]) <> Pdef(\longverb),

		])
);

Pdef(\player2,
	Ppar([
		Pbindf(
			Pdef(\percPattern2),
			\out, [~convolve_B]
		),

		Pbindf(
			Pdef(\fmPercPatt3),
			\freq, Pseq([24, 23],inf).stutter(12).midicps,
			\gain, -24,
			\out, [~convolve_A, ~convolve_B]
		),

		Pbindf(
			Pdef(\segPattern),
			\out, [~mainout, ~convolve_A, ~convolve_B, ~segPattern_out]
		),

		Pbind(\out, [~mainout, ~sub_out])<>Pdef(\subPattern),
		Pbindf(
			Pdef(\convolve),
			\mix, 1,
			\gain, -12,
			\framesize, 4096,
			\hpfIn, 100,
			\hpfOut, 80,
			\out, [~mainout, ~longverb, ~convolution_out]
		),
		Pbind(\out, [~mainout, ~longverb_out]) <> Pdef(\longverb),
		])
);

Pdef(\player3,
	Ppar([
		Pbindf(
			Pdef(\percPattern2),
			\out, [~convolve_B]
		),

		Pbindf(
			Pdef(\fmPad4),
			\out, [~mainout, ~convolve_A, ~convolve_B, ~longverb, ~fmPad_out]
		),

		Pbindf(
			Pdef(\fmPercPatt3),
			\freq, Pseq([24, 23],inf).stutter(12).midicps,
			\gain, -24,
			\out, [~convolve_A, ~convolve_B]
		),

		Pbindf(
			Pdef(\segPattern),
			\out, [~mainout, ~convolve_A, ~echo, ~segPattern_out]
		),

		Pbind(\out, [~mainout, ~sub_out]) <> Pdef(\subPattern2),

		Pbindf(
			Pdef(\convolve),
			\mix, 1,
			\gain, -12,
			\framesize, 4096,
			\hpfIn, 100,
			\hpfOut, 80,
			\out, [~mainout, ~longverb, ~convolution_out]
		),

		Pbind(\out, [~mainout, ~longverb_out])<>Pdef(\longverb),
		Pbind(\out, [~mainout, ~delay_out])<>Pdef(\delay),
		])
);

Pdef(\player4,
	Ppar([
		Pbindf(
			Pdef(\percPattern2),
			\gain, -12,
			\out, [~mainout, ~convolve_A, ~convolve_B, ~percPattern_out]
		),

		Pbindf(
			Pdef(\fmPad4),
			\out, [~mainout, ~convolve_A, ~convolve_B, ~longverb, ~fmPad_out]
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
			\out, [~mainout, ~delay, ~segPattern_out]
		),

		Pbind(\out, [~mainout, ~sub_out])<>Pdef(\subPattern2),

		Pbindf(
			Pdef(\convolve),
			\mix, 1,
			\gain, -12,
			\framesize, 4096,
			\hpfIn, 100,
			\hpfOut, 80,
			\out, [~mainout, ~longverb, ~convolution_out]
		),

        Pbind(\out, [~mainout, ~longverb_out])<>Pdef(\longverb),
		Pbind(\out, [~mainout, ~delay_out])<>Pdef(\delay),
		])
);

Pdef(\player5,
	Ppar([

		Pbindf(
			Pdef(\percPattern2),
			\gain, -12,
			\out, [~mainout, ~convolve_A, ~convolve_B, ~percPattern_out]
		),

		Pbindf(
			Pdef(\fmPad2),
            \gain, 40,
			// \gain, 0,
			\out, [~mainout, ~convolve_A, ~fmPad_out]
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
			\out, [~mainout, ~convolve_A, ~convolve_B, ~delay, ~segPattern_out]
		),

		Pbind(\out, [~mainout, ~sub_out]) <> Pdef(\subPattern3),

		Pbindf(
			Pdef(\convolve),
			\mix, 0.9,
			\gain, -6,
			\framesize, 4096,
			\out, [~mainout, ~convolution_out]
		),

        Pbind(\out, [~mainout, ~longverb_out])<>Pdef(\longverb),
		Pbind(\out, [~mainout, ~delay_out])<>Pdef(\delay),

		])
);

Pdef(\player6,
	Ppar([
		Pbindf(
			Pdef(\percPattern5),
			\gain, -12,
			\out, [~mainout, ~convolve_A, ~convolve_B, ~percPattern_out]
		),
		Pbindf(
			Pdef(\fmPad2),
			\out, [~mainout, ~convolve_A, ~convolve_B, ~longverb, ~fmPad_out]
		),

		Pbindf(
			Pdef(\fmPercPatt3),
			\freq, Pseq([24, 23],inf).stutter(12).midicps,
			\gain, -24,
			\out, [~mainout, ~fmPerc_out, ~fmPerc_out],
		),

		Pbindf(
			Pdef(\clapPattern),
			\out, [~convolve_A, ~convolve_B]
		),

		Pbindf(
			Pdef(\segPattern2),
			\out, [~mainout, ~convolve_A, ~convolve_B, ~segPattern_out, ~segPattern_out]
		),

		Pbind(\out, [~mainout, ~sub_out]) <> Pdef(\subPattern3),

		Pbindf(
			Pdef(\convolve),
			\mix, 1,
			\gain, -12,
			\framesize, 4096,
			\hpfIn, 100,
			\hpfOut, 80,
			\out, [~mainout, ~longverb, ~convolution_out]
		),

        Pbind(\out, [~mainout, ~longverb_out])<>Pdef(\longverb),
		Pbind(\out, [~mainout, ~delay_out])<>Pdef(\delay),
		])
	);

Pdef(\player7,
	Ppar([
		Pbindf(
			Pdef(\percPattern5),
			\out, [~mainout, ~mod1_in, ~modDelay, ~percPattern_out]
		),
		Pbindf(
			Pdef(\fmPad4),
            \out, [~mainout, ~fmPad_out]
		),

		Pbindf(
			Pdef(\fmPercPatt1),
			\freq, Pseq([24, 23],inf).stutter(12).midicps,
			\out, [~mainout, ~delay, ~fmPerc_out]
		),

		Pbindf(
			Pdef(\clapPattern),
			\out, [~mainout, ~delay, ~clap_out]
		),
		Pbindf(
			Pdef(\segPattern2),
			\out, [~mainout, ~delay, ~segPattern_out]
		),

		Pbind(\out, [~mainout, ~sub_out]) <> Pdef(\subPattern3),

        Pdef(\follower1),
		Pbind(\out,[~mainout, ~modDelay_out])<>Pdef(\modDelay),
		])
);

//this
Pdef(\player8,
	Ppar([
		Pbindf(
			Pdef(\percPattern3),
			\out, [~mainout, ~percPattern_out]
		),
		Pbindf(
			Pdef(\fmPad2),
            \out, [~mainout, ~fmPad_out]
		),
		Pbind(\out, [~mainout, ~fmPerc_out]) <> Pdef(\fmPercPatt2),
		Pbind(\out, [~mainout, ~kick_out]) <> Pdef(\kickPattern1),
		Pbindf(
			Pdef(\clapPattern),
			\out, [~mainout, ~delay, ~clap_out]
		),
		Pbindf(
			Pdef(\segPattern2),
			\out, [~mainout, ~delay, ~segPattern_out]
		),
		Pbind(\out, [~mainout, ~sub_out]) <> Pdef(\subPattern),
		Pbind(\out, [~mainout, ~longverb_out]) <> Pdef(\longverb),
		Pbind(\out, [~mainout, ~delay_out]) <> Pdef(\delay),
		])
);

Pdef(\player9,
	Ppar([
		Pbind(\out, [~mainout, ~percPattern_out]) <> Pdef(\percPattern2),

		Pbindf(
			Pdef(\fmPad4),
			\gain, 40,
            \out, [~mainout, ~fmPad_out]
		),
		Pbind(\out, [~mainout, ~fmPerc_out]) <> Pdef(\fmPercPatt1),
		Pbind(\out, [~mainout, ~clap_out]) <> Pdef(\clapPattern),
		Pbind(\out, [~mainout, ~segPattern_out]) <> Pdef(\segPattern2),
		Pbind(\out, [~mainout, ~sub_out]) <> Pdef(\subPattern3),
		])
);

Pdef(\player10,
	Ppar([
		Pbind(\out, [~mainout, ~percPattern_out]) <> Pdef(\percPattern5),

		Pbindf(
			Pdef(\segPattern),
			\out, [~mainout, ~delay, ~segPattern_out]
		),
		Pbindf(
			Pdef(\fmPad4), 
            \freq, 15,
            \out, [~mainout, ~fmPad_out]
		),

		Pbind(\out, [~mainout, ~clap_out]) <> Pdef(\clapPattern),
		Pbind(\out, [~mainout, ~kick_out]) <> Pdef(\kickPattern1),
		Pbindf(
			Pdef(\fmPercPatt4),
			\out, [~mainout, ~mod1_in, ~modDelay, ~fmPerc_out]
		),

		Pbindf(
			Pdef(\hhPattern1),
			\out, [~mainout, ~mod1_in, ~hh_out]
		),

		Pbind(\out, [~mainout, ~sub_out]) <> Pdef(\subPattern),
		Pbind(\out, [~mainout, ~delay_out]) <> Pdef(\delay),
		
        Pdef(\follower1),
		
        Pbindf(
			Pdef(\modDelay),
			\lpf, 5000,
			\size, 0.0095,
            \out, [~mainout, ~modDelay_out]
		)
		])
);

Pdef(\player11,
	Ppar([
		Pbind(\out, [~mainout, ~percPattern_out]) <> Pdef(\percPattern5),
		Pbindf(
			Pdef(\fmPad),
			\gain, -6,
			\freq, 30,
            \out, [~mainout, ~fmPad_out]
		),
		Pbind(\out, [~mainout, ~sub_out]) <> Pdef(\subPattern),
		Pbind(\out, [~mainout, ~fmPerc_out]) <> Pdef(\fmPercPatt4),
		Pbind(\out, [~mainout, ~clap_out]) <> Pdef(\clapPattern),
		Pbind(\out, [~mainout, ~kick_out]) <> Pdef(\kickPattern1),
		Pbind(\out, [~mainout, ~hh_out]) <> Pdef(\hhPattern1)
		])
);

Pdef(\player12,
	Ppar([
		Pbindf(
			Pdef(\percPattern2),
			\out, [~mainout, ~convolve_A, ~convolve_B, ~percPattern_out]
		),
		Pbindf(
			Pdef(\fmPad3),
			\gain, 10,
            \out, [~mainout, ~fmPad_out]
		),
		Pbind(\out, [~mainout, ~sub_out]) <> Pdef(\subPattern3),
		Pbindf(
			Pdef(\fmPercPatt2),
			\out, [~mainout, ~convolve_A, ~convolve_B, ~longverb, ~fmPerc_out]
		),
		Pbind(\out, [~mainout, ~clap_out]) <> Pdef(\clapPattern),
		Pbind(\out, [~mainout, ~kick_out]) <> Pdef(\kickPattern1),
		// Pdef(\hhPattern1),
		Pbindf(
			Pdef(\convolve),
			\mix, 1,
			\gain, -12,
			\framesize, 4096,
			\hpfIn, 100,
			\hpfOut, 80,
			\out, [~mainout, ~longverb, ~convolution_out]
		),
		Pbind(\out, [~mainout, ~longverb_out]) <> Pdef(\longverb),
		])
);

Pdef(\player12,
	Ppar([
		Pbindf(
			Pdef(\percPattern2),
			\out, [~mainout, ~convolve_A, ~convolve_B, ~percPattern_out]
		),
		Pbindf(
			Pdef(\fmPad3),
			\gain, 10,
            \out, [~mainout, ~fmPad_out]
		),
		Pbind(\out, [~mainout, ~sub_out]) <> Pdef(\subPattern3),
		Pbindf(
			Pdef(\fmPercPatt2),
			\out, [~mainout, ~convolve_A, ~convolve_B, ~longverb, ~fmPerc_out]
		),
		Pbind(\out, [~mainout, ~clap_out]) <> Pdef(\clapPattern),
		Pbind(\out, [~mainout, ~kick_out]) <> Pdef(\kickPattern1),
		// Pdef(\hhPattern1),
		Pbindf(
			Pdef(\convolve),
			\mix, 1,
			\gain, -12,
			\framesize, 4096,
			\hpfIn, 100,
			\hpfOut, 80,
			\out, [~mainout, ~longverb, ~convolution_out]
		),
		Pbind(\out, [~mainout, ~longverb_out]) <> Pdef(\longverb),
		])
);

Pdef(\player13,
	Ppar([
		Pbindf(
			Pdef(\fmPad4),
            \out, [~mainout, ~fmPad_out]
			// \gain, 10
		),
	])
);
)


Pdef(\player13).play(t, quant:[1,0]);
