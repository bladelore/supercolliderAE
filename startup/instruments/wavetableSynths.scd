(
    SynthDef(\fbWt_single, {|buffer=#[0,1]|

        var sig, currentSample, fbIdx;

        var globalLag = \globalLag.kr(0);

        var atk = Lag.kr(\atk.kr(0.05), globalLag);
        var dec = Lag.kr(\dec.kr(0.2), globalLag);
        var sus = Lag.kr(\sus.kr(0), globalLag);
        var rel = Lag.kr(\rel.kr(0), globalLag);

        var size = Lag.kr(\size.kr(128), globalLag);
        var fb = Lag.kr(\fb.kr(0), globalLag);

        var sweep = Lag.kr(\sweep.kr(1.5), globalLag);

        var pitchEnv = 1 + (
            sweep * 
            Env.perc(0.0, 0.13, curve: -4).ar * 
            XLine.ar(1, 0.6, dec)
        );

        var freq = Lag.kr(\freq.kr(1) * SampleDur.ir, globalLag);
        var cycleRate = Lag.kr(\cycleRate.kr(0.1), globalLag);
        // var cyclePos = SinOsc.ar(cycleRate, 2pi).linlin(-1, 1, 0, 1);
        var cyclePos = LFNoise2.ar(cycleRate).linlin(-1, 1, 0, 1);
        var phase = Phasor.ar(DC.ar(0), freq);
        var cycleOffset = Lag.kr(\cycleOffset.kr(0), globalLag);
        var stereoOffset = Lag.kr(\stereoOffset.kr(0.1), globalLag);

        freq = freq * pitchEnv;

        sig = buffer.collect({|buf, channel|
            sig = SingleOscOS.ar(
                bufnum: buf,
                phase: phase,
                numCycles: BufFrames.kr(buf) / size,
                cyclePos: (cyclePos + cycleOffset + channel.linlin(0, 1, 0, stereoOffset * 2pi)).wrap(0,1),
                // cyclePos: cyclePos,
                oversample: 4,
            );
            
            fbIdx = cyclePos * BufFrames.kr(buf);
            currentSample = BufRd.ar(1, buf, fbIdx);
            currentSample = ((sig * fb) + currentSample);
            // currentSample = brf.ar(currentSample, 15000);
            fbIdx = Select.ar(fb > 0, [DC.ar(-1), fbIdx]);
            BufWr.ar(LeakDC.ar(currentSample), buf, fbIdx);
            sig = sig.tanh();
            LeakDC.ar(sig) * \gain.kr(-6).dbamp;
        });

        sig = sig * EnvGen.ar(Env.adsr(atk, dec, sus, rel), \gate.kr(0.5), doneAction: 2);

        sig = sig.blend(BRF.ar(sig, \brf.kr(500), 2), \filterMix.kr(1));

        sig = RLPF.ar(sig, \lpf.kr(15000));

        sig = sig.clip; 

        Out.ar(\out.kr(0), sig);
    }).add;
)