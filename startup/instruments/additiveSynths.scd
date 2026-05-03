(
    SynthDef(\additivePerc, {
        var sig, env;
        var atk = \atk.kr(0.01);
        var dec = \dec.kr(1);
        var gate = \gate.kr(1);
        var oneshot  = \oneshot.kr(1);
        var bufs     = \wt.kr(0 ! 4);
        var slice    = \slice.kr(0 ! 2);
        var start    = slice[0];
        var end      = slice[1];
        var rate     = \rate.kr(1);
        var durSamps = (end - start) * bufs[3] * rate;
        var durSecs  = durSamps / SampleRate.ir * rate;
        var phasor   = Phasor.ar(0, 1 / durSamps);
        var line     = Sweep.ar(1, 1 / durSecs).min(1);
        var phase    = Select.ar(oneshot, [phasor, line]);
        var resonator, resFreq, resAtk, resDec;

        var scale = Scale.major.tuning_(\just);

        durSecs = (dec * durSecs).min(2);        

        sig = AdditiveReader(freqBuf: bufs[0], ampBuf: bufs[1], phaseBuf: bufs[2], hopSize: bufs[3], numPartials: 32)
            .readPhase(phase, startFrame: start, endFrame: end)
            .hpFilter(50)
            .lpFilter(\lpf.kr(2000), 4)
            .ampAbove(\ampThresh.kr(0))
            .evenOddMask(\odd.kr(1), \even.kr(1))
            .quantizePartials(scale, \quantize.kr(0.8), 60.midicps)
            .ampSlew(\ampAtk.kr(0), \ampRel.kr(0))
            .ampNormalise(-50, \flatten.kr(0))
            .spectralTilt(\tilt.kr(0))
            .transpose(\transpose.kr(0))
            .limiter
            .oscBank
            .midSideSpread(\spread.kr(1))
        ;

        sig = sig * 0.01;
        
        env = Env.adsr(attackTime: \atk.kr(0.01), decayTime: durSecs, sustainLevel: \sus.kr(0.5), releaseTime: \rel.kr(0));
        env = EnvGen.kr(env, gate, doneAction: oneshot.linlin(0,1,0,2));
        env = Select.kr(oneshot, [1, env]);
        sig = sig * env;

        //resonator
        resFreq = \resFreq.kr(50);
        resAtk = \resAtk.kr(0.01);
        resDec = \resDec.kr(0.25) + resAtk;
        resonator = Formlet.ar(sig * \resInGain.kr(-30).dbamp, resFreq, resAtk, resDec);
        resonator = (resonator * \resDistort.kr(1)).tanh * \resOutGain.kr(-12).dbamp * \resAmp.kr(1);
        sig = sig + resonator;

        sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
        sig = sig * \gain.kr(0).dbamp;
        sig = sig * \amp.kr(1);
        sig = sig.sanitize;
        
        Out.ar(\out.kr(0), sig);
    }).add;

    Ndef(\additiveWavetablePad, {
        var sig;
        var bufs = \wt.kr(0 ! 4);
        var scale = Scale.minor.tuning_(\just);

        var phase = Phasor.ar(0, BufFrames.kr(bufs[0]) / (s.sampleRate * \rate.kr(5)));
        // var phase = LFNoise2.ar(BufFrames.kr(~wt.freqBuf) / (s.sampleRate * 10)).linlin(-1, 1, 0, 1);

        sig = AdditiveReader(bufs[0], bufs[1], bufs[2], bufs[3], 12)
            .readPhase(phase, startFrame: \startFrame.kr(200), endFrame: \endFrame.kr(201))
            .transpose(\transpose.kr(-12))
            .hpFilter(LFNoise2.ar(0.1).exprange(5, 4000))
            .spectralTilt(LFNoise2.ar(0.1).range(6, -6))
            .ampSlew(\ampAtk.kr(10), \ampRel.kr(10))
            .ampAbove(\ampThresh.kr(0.6))
            .quantizePartials(scale, \quantize.kr(0.8), 30.midicps)
            .ampSlew(\ampAtk.kr(10), \ampRel.kr(10))
            .hpFilter(\hpFilter.kr(50))
            .lpFilter(\lpFilter.kr(1000))
            // .ampNormalise(-50, \flatten.kr(0))
            .limiter
            .oscBank(randomPhase: 1)
            .air(amount: 1, speed: \airSpeed.kr(0.8), min: 0.1, max: 0.9)
            .render
        ;

        sig = sig * 0.01;
        sig = sig * \amp.kr(1).lag;
        sig = sig * \gain.kr(0).dbamp;
        // sig = sig.sanitize;
    });
)