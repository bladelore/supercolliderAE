(
	var sampleA;

	Ndef(\sample).clear;
	Ndef(\sample2).clear;

	~new_advance.();
	x = {
			\a.postln;
			
		    b = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./matchstick burning/burning water.wav");

			Ndef(\sample).fadeTime = 10;
            Ndef(\sample,
                {
                    var sig;
                    var buf = \buf.kr(0);
                    var pos = \pos.kr(0) * BufFrames.kr(buf);
                    var harmL, percL, residualL;
                    var harmR, percR, residualR;
                    var fluid;
                    var sinesL, sinesR, sines;

                    sig = PlayBuf.ar(2, buf, startPos: pos, rate: \rate.kr(0) * BufRateScale.kr(buf), loop: \loop.kr(0), trigger: Impulse.kr(0) + Changed.kr(pos + buf));
                    # harmL, percL, residualL = FluidHPSS.ar(sig[0], 17, 31, maskingMode:1);
                    # harmR, percR, residualR = FluidHPSS.ar(sig[1], 17, 31, maskingMode:1);

                    fluid = [harmL, harmR];
                    
                    sig = SelectX.ar(\source.kr(1), [sig, fluid]) * \gain.kr(0).dbamp;
                }
			);

			Ndef(\warp).fadeTime = 10;
            Ndef(\warp,
                {
					var harmL, percL, residualL;
                    var harmR, percR, residualR;
                    var fluid;
                    var sinesL, sinesR, sines;

                    var sig;
                    var buf = \buf.kr(0);
					var bufFrames = BufFrames.kr(buf);
					var pos      = \pos.kr(0);           // normalised 0–1
					var posRate  = \posRate.kr(1);

					var pointer = Phasor.ar(
						rate:     BufRateScale.kr(buf) * posRate / bufFrames,
						start:    pos,
						end:      1.0,
						resetPos: pos
					);

					sig = WarpZ.ar(2, buf, pointer, \rate.kr(1) * BufRateScale.kr(buf), windowSize: \windowSize.kr(0.1), overlaps: 16, windowRandRatio: 0.5, interp: 4, zeroSearch: 1, zeroStart: 1);                    
					# harmL, percL, residualL = FluidHPSS.ar(sig[0], 17, 31, maskingMode:1);
                    # harmR, percR, residualR = FluidHPSS.ar(sig[1], 17, 31, maskingMode:1);

                    fluid = [harmL, harmR];
                    
                    sig = SelectX.ar(\source.kr(1), [sig, fluid]) * \gain.kr(0).dbamp;
                }
			);

			Ndef(\sample)[999] = \pset -> Pbind(\rate, ~knob.(0).linlin(0,1,0.5,2), \source, ~knob.(1), \dur, 0.01);
            Ndef(\sample).set(\buf, b, \pos, 0, \rate, 1, \loop, 1, \gain, 0).play(~bus3);

			Ndef(\sample).copy(\sample2);

		~advance.wait;

			\b.postln;

			b =Buffer.read(s, "/Users/aelazary/Projects/guitars 2026 Project/guitars 2026-1.wav");

			Ndef(\sample).stop;
			Ndef(\warp).xset(\buf, b, \pos, 0, \rate, 1, \loop, 1, \gain, 0).play(~bus3);
			Ndef(\warp)[999] = \pset -> Pbind(\rate, 1, \posRate, ~knob.(1), \windowSize, ~knob.(2).linlin(0,1,0.01,0.25),  \source, ~knob.(3), \dur, 0.01);

		~advance.wait;

			\c.postln;

			b =Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./matchstick burning/burning 3.wav");

			Ndef(\sample).xset(\buf, b, \pos, 0, \rate, 1, \loop, 1, \gain, 0).play(~bus3);

		~advance.wait;

			\d.postln;

			c = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./serum resamples/choirspec.wav");
			
			Ndef(\sample2).fadeTime = 10;
			Ndef(\sample2).xset(\buf, c, \pos, 0, \rate, 1, \loop, 0, \gain, 0).play(~bus3);

			Ndef(\sample2)[999] = \pset -> Pbind(\rate, 1, \source, ~knob.(2), \dur, 0.01);
			
		~advance.wait;

			\e.postln;

			b =Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./matchstick burning/burning water.wav");

			Ndef(\sample).xset(\buf, b, \pos, 0, \rate, 1, \loop, 1, \gain, 0).play(~bus3);

		~advance.wait;

			\f.postln;

			d = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./Field recs/drunk in marseille.wav");

			Ndef(\sample2).xset(\buf, d, \pos, 0, \rate, 1, \loop, 1, \gain, 0).play(~bus1);

		~advance.wait;

			Ndef(\sample).stop(fadeTime: 10);

	}.fork;
)

Ndef(\sample2).stop(fadeTime: 10);

~test.()

