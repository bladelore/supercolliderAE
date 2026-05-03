(
	var player;
	var sampleA;

	Ndef(\sample).clear;
	Ndef(\sample2).clear;

	player = Conductor(\player, t);
    player.quant_(0);
    player.targetSection_(nil);
    player.listen((type: \modality, device: k, key: \tr, button: \fwd));
	
	x = {

		player.label;
			
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
                    
                    sig = SelectX.ar(\source.kr(1), [sig, fluid]) * \gain.kr(0).dbamp * \amp.kr(0).lag; 
                }
			);

			Ndef(\sample)[999] = \pset -> Pbind(\amp, ~slider.(0), \rate, ~knob.(0).linlin(0,1,0.5,2), \source, ~knob.(1), \dur, 0.01);

            Ndef(\sample).set(\buf, b, \pos, 0, \rate, 1, \loop, 1, \gain, 0).play(~bus3);

			Ndef(\sample).copy(\sample2);

		player.wait; player.label;

			b =Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./matchstick burning/burning 4.wav");

			Ndef(\sample).xset(\buf, b, \pos, 0, \rate, 1, \loop, 1, \gain, 0).play(~bus3);

		player.wait; player.label;

			b =Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./matchstick burning/burning 3.wav");

			Ndef(\sample).xset(\buf, b, \pos, 0, \rate, 1, \loop, 1, \gain, 0).play(~bus3);

		player.wait; player.label;

			// c = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./serum resamples/choirspec.wav");
			c = Buffer.read(s, "/Users/aelazary/Projects/guitars 2026 Project/guitars 2026-1.wav");
			
			Ndef(\sample2).fadeTime = 10;
			Ndef(\sample2).xset(\buf, c, \pos, 0.75, \rate, 1, \loop, 0, \gain, 10).play(~bus3);

			Ndef(\sample2)[999] = \pset -> Pbind(\rate, 1, \source, ~knob.(2), \dur, 0.01);
			
		player.wait; player.label;

			b =Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./matchstick burning/burning water.wav");

			Ndef(\sample).xset(\buf, b, \pos, 0, \rate, 1, \loop, 1, \gain, 0).play(~bus3);

		// player.wait; player.label;

			// d = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./Field recs/drunk in marseille.wav");

			// Ndef(\sample2).xset(\buf, d, \pos, 0, \rate, 1, \loop, 1, \gain, 0).play(~bus1);

		// player.wait; 
		player.label(\end);

			Ndef(\sample).stop(fadeTime: 10);

	}.fork;
)

Ndef(\sample2).stop(fadeTime: 10);