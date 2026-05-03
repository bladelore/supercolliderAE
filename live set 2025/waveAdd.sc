(
    // a = "/Users/aelazary/Desktop/Samples etc./Field recs/Athens Bell Parthenon.wav";

    // a = "/Users/aelazary/Desktop/Samples etc./Field recs/drunk in marseille.wav";
    
    a = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./NEW sample lib/flow of dust-e7R-6jxBmnc.wav");
    b = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./Field recs/Athens Bell Parthenon.wav");
    c = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./john pork.wav");

    AdditiveWavetable.analyse(s, a, numPartials: 32, windowSize: 1024, action: { |wt| ~wt1 = wt; ~wt1.loadBuffers(s);});
    AdditiveWavetable.analyse(s, b, numPartials: 32, windowSize: 1024, action: { |wt| ~wt2 = wt; ~wt2.loadBuffers(s);});
    AdditiveWavetable.analyse(s, c, numPartials: 32, windowSize: 1024, action: { |wt| ~wt3 = wt; ~wt3.loadBuffers(s);});
)

(
    Ndef(\additiveWavetablePad).clear;

    Ndef(\additiveWavetablePad, {
        var sig;
        var bufs = \wt.kr(0 ! 4);
        var scale = Scale.minor.tuning_(\just);

        // var phase = Phasor.ar(0, BufFrames.kr(bufs[0]) / (s.sampleRate * \rate.kr(5)));
        var phase = LFNoise2.ar(BufFrames.kr(bufs[0]) / (s.sampleRate * \rate.kr(5))).linlin(-1, 1, 0, 1);

        sig = AdditiveReader(bufs[0], bufs[1], bufs[2], bufs[3], 32)
            .readPhase(phase, startFrame: \startFrame.kr(200), endFrame: \endFrame.kr(201))
            .transpose(\transpose.kr(-12))
            .hpFilter(LFNoise2.ar(0.1).exprange(5, 4000))
            .spectralTilt(LFNoise2.ar(0.1).range(6, -6))
            // .ampSlew(\ampAtk.kr(10), \ampRel.kr(10))
            .ampAbove(\ampThresh.kr(0.6))
            .quantizePartials(scale, \quantize.kr(0.8), 30.midicps)
            
            .hpFilter(\hpFilter.kr(50))
            .lpFilter(\lpFilter.kr(1000))
            // .ampNormalise(-50, \flatten.kr(0))
            
            .ampSlew(\ampAtk.kr(10), \ampRel.kr(10))
            .morphFilter(500, morph: \morph.kr(0).lag)
            .limiter
            .oscBank(randomPhase: 1)
            // .air(amount: 1, speed: \airSpeed.kr(0.8), min: 0.1, max: 0.9)
            .midSideSpread(\spread.kr(1).lag)
            .render
        ;

        sig = sig * 0.01;
        
        sig = sig * \amp.kr(1).lag;
        sig = sig * \gain.kr(0).dbamp.lag;
        // sig = sig.clip;
        // sig = sig.sanitize;
    });

    Ndef(\sineTracker).clear;

    Ndef(\sineTracker, {
        var sig, chain, sample, src, follower, fbIn;
        var buf = \buf.kr(0);
        var slew = \slew.kr(0);
        // feedback
        fbIn   = LocalIn.ar(2) * \feedback.kr(0.5);
        sample = PlayBuf.ar(1, buf, BufRateScale.kr(buf), loop: 1);
        src    = sample + fbIn;

        sig = AdditiveChain(12, 440)
            .extractSines(
                src,
                freqLag:  \freqLag.kr(0.01),
                ampLag:   \ampLag.kr(0.1),
                order:    1,
                transpose: \transpose.kr(0),
                winSize:  1024,
                fftSize:  1024,
                hopSize:  4
            )
            
            .notchFilter(SinOsc.ar(0.5).linlin(-1, 1, 15000, 1000), 1000)
            .ampAbove(\ampThresh.kr(0.6))
            .ampSlew(slew, slew)
            .limiter
            .oscBank(randomPhase: 1)
            .stereoSpread( amount: SinOsc.ar(0.5).unipolar, ramp: 1, saw: SinOsc.ar(3).unipolar, cycles: 1)
            .render;

        // sig = Balance2.ar(sig[0], sig[1]).sum;

        sig = Compander.ar(sig, sig,
            thresh:     0.5,
            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );

        LocalOut.ar(sig.sanitize);

        sig = sig.blend(sample,  1 - \drywet.kr(0));
        sig = sig * \gain.kr(0).dbamp.lag;
        sig = sig * \amp.kr(1).lag;
});

    ~zerox =Bus.audio(s,2);

    Ndef(\zerox).clear;
    Ndef(\zerox, {
        var in, zerox, pan, sig;

        in = InFeedback.ar(\inbus.kr(0), 1);

        zerox = ZeroCrossing.ar(in) / \divisor.kr(16);
        zerox = Impulse.ar(zerox);

        pan = Latch.ar(WhiteNoise.ar(\width.kr(1)), zerox);

        sig = Pan2.ar(zerox, pan);

        sig = sig * \gain.kr(0).dbamp.lag;
        sig = sig * \amp.kr(1).lag;
    });
)

~wt1.numFrames

(
    var player;
    var buf;
    player = Conductor(\player, t);
    player.quant_(0);
    player.targetSection_(nil);
    player.listen((type: \modality, device: k, key: \tr, button: \fwd));
    
    x = {

        player.label;

            Ndef(\additiveWavetablePad).fadeTime = 15;

            Ndef(\additiveWavetablePad).set(\wt, ~wt1.asControlInput).play([~bus1.channels, ~zerox.channels].flatten);

            Ndef(\additiveWavetablePad).set(\transpose, -12, \gain, 0);

            Ndef(\additiveWavetablePad)[999] = \pset -> Pbind(
                \gain, ~knob.(0) * 30,
                \amp, 1,
                \dur, 0.01,
                \ampAtk, 0.1,
                \ampRel, 0.1,
                \flatten, 0,
                \morph, ~knob.(1),
                \rate, ~knob.(2).linexp(0,1, 0.1, 1),
                \quantize, ~knob.(3),   
                \spread, ~knob.(4),
                \startFrame, ~knob.(5).linlin(0,1,0,~wt1.numFrames - 1),
                \endFrame, ~knob.(6).linlin(0,1,0,~wt1.numFrames - 1),
                \ampThresh, 1 - ~knob.(7)
            );

            
            Ndef(\zerox).set(\inbus, ~zerox).play(~bus3);
            Ndef(\zerox)[999] = \pset -> Pbind(\dur, 0.01, \amp, ~slider.(7));

            buf = Buffer.readChannel(s, "/Users/aelazary/Desktop/Samples etc./spirographshr/Spirograph ｜ by ShR [KukuDQEm-Vc].wav", channels: [0]);            
            Ndef(\sineTracker).set(\buf, buf, \gain, -15, \transpose, -12);
            Ndef(\sineTracker).play(~bus2);

            Ndef(\sineTracker)[999] = \pset -> Pbind(
                \dur, 0.01,
                \amp, ~slider.(0), 
                \drywet, ~slider.(1),
                \feedback, ~slider.(2),
                \slew, ~slider.(3).linlin(0,1,0, 2),
                \ampThresh, 0.2
            );


        player.wait;

        player.label;

            buf = Buffer.readChannel(s, "/Users/aelazary/Desktop/Samples etc./Field recs/doha_quiet.wav", channels: [0]);            
            Ndef(\sineTracker).set(\buf, buf, \gain, -15, \transpose, -12);

            Ndef(\additiveWavetablePad).xset(\wt, ~wt2.asControlInput, \transpose, 0).play([~bus1.channels, ~zerox.channels].flatten);

            Ndef(\additiveWavetablePad)[999] = \pset -> Pbind(
                \gain, ~knob.(0) * 30,
                \amp, 1,
                \dur, 0.01,
                \ampAtk, 0.1,
                \ampRel, 0.1,
                \flatten, 0,
                \morph, ~knob.(1),
                \rate, ~knob.(2).linexp(0,1, 0.1, 1),
                \quantize, ~knob.(3),   
                \spread, ~knob.(4),
                \startFrame, ~knob.(5).linlin(0,1,0,~wt2.numFrames - 1),
                \endFrame, ~knob.(6).linlin(0,1,0,~wt2.numFrames - 1),
                \ampThresh, 1 - ~knob.(7)
            );

        player.wait;

        player.label;

            Ndef(\additiveWavetablePad).xset(\wt, ~wt3.asControlInput, \transpose, -12).play([~bus1.channels, ~zerox.channels].flatten);

            Ndef(\additiveWavetablePad)[999] = \pset -> Pbind(
                \gain, ~knob.(0) * 30,
                \amp, 1,
                \dur, 0.01,
                \ampAtk, 0.1,
                \ampRel, 0.1,
                \flatten, 0,
                \morph, ~knob.(1),
                \rate, ~knob.(2).linexp(0,1, 0.1, 1),
                \quantize, ~knob.(3),   
                \spread, ~knob.(4),
                \startFrame, ~knob.(5).linlin(0,1,0,~wt3.numFrames - 1),
                \endFrame, ~knob.(6).linlin(0,1,0,~wt3.numFrames - 1),
                \ampThresh, 1 - ~knob.(7)
            );

        player.wait;

        player.label;

            buf = Buffer.readChannel(s, "/Users/aelazary/Desktop/Samples etc./Field recs/Athens Bell Parthenon.wav", channels: [0]);            
            Ndef(\sineTracker).set(\buf, buf, \gain, -15, \transpose, -12);

            Ndef(\additiveWavetablePad).xset(\wt, ~wt1.asControlInput, \transpose, -48).play([~bus1.channels, ~zerox.channels].flatten);

            Ndef(\additiveWavetablePad)[999] = \pset -> Pbind(
                \gain, ~knob.(0) * 30,
                \amp, 1,
                \dur, 0.01,
                \ampAtk, 0.1,
                \ampRel, 0.1,
                \flatten, 0,
                \morph, ~knob.(1),
                \rate, ~knob.(2).linexp(0,1, 0.1, 1),
                \quantize, ~knob.(3),   
                \spread, ~knob.(4),
                \startFrame, ~knob.(5).linlin(0,1,0,~wt1.numFrames - 1),
                \endFrame, ~knob.(6).linlin(0,1,0,~wt1.numFrames - 1),
                \ampThresh, 1 - ~knob.(7)
            );

    }.fork;
)

(
    Ndef(\additiveWavetablePad).stop(fadeTime: 10);
    Ndef(\sineTracker).stop(fadeTime: 10);
    Ndef(\zerox).stop(fadeTime: 10);
)