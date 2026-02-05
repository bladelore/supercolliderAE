(

    ~specBuff = Dictionary();
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./ZOOM H5/Coburg bush reserve/Playground vibraphone.wav", ~specBuff, 16384, 2);
    ~new_advance.();
    x = {
        
        \ipf_A.postln;

        Pdef(\specSample,
            Pmono(\fftStretch_magAbove_mono,
                \dur, 0.01,
                \amp, 1,
                \gain, 0,
                \buf, ~specBuff.at(\file),
                \analysis, [~specBuff.at(\analysis)],
                \fftSize, ~specBuff.at(\fftSize),
                \pitchRatio, 1,
                \rate, ~knob.(4),
                \filter, 1 - ~knob.(5),
                \pos, ~knob.(6),
                \len, 0.2,
                \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
                \out, [~convolve_B]
            )
        ).play(t);

        Ndef(\morph, \cepstralMorph_fx).set(\inbus_A, ~convolve_A, \inbus_B, ~convolve_B);
        Ndef(\morph)[999] = \pset -> Pbind(
            \amp, 1,
            \dur, 0.01,
            \gain, -10,
            \atk, 10,
            \rel, 100,
            \swap, ~knob.(7),
            \drywet, 1,
        );
        Ndef(\morph).play(~bus2);

        // IPF multiphonics A

        Ndef(\ipf).clear;
        Ndef(\ipf).fadeTime = 5;

        Ndef(\ipf, {
            var trig, g_prev, g, su, safeVal, g_out, md, freq, sig;
            var verb_time, verb_damp;
            
            var f0 = \f0.kr(60);
            var alpha = SinOsc.ar(\alphaRate.kr(0.5)).linlin(-1, 1, 0.01, 9);
            var beta = \beta.kr(0.3);
            var g_init = \g_init.kr(0.6);
            var modRate = \modRate.kr(1);
            var modStereo=\modStereo.kr(100);

            trig = Impulse.ar(f0);
            g_prev = LocalIn.ar(2);
            g = Select.ar((trig > 0), [K2A.ar(g_init), g_prev]);

            su = beta * exp(g - g_prev);
            safeVal = ((g - su) / alpha).max(0.00001);
            g_out = g - log(safeVal);

            LocalOut.kr(g_out);

            // md = [modRate, modRate + modStereo];
            md = [modRate - (modStereo * 0.5), modRate + (modStereo * 0.5)];
            freq = f0 / (1 + g_out * md).max(0.001);

            sig = SinOsc.ar(freq);

            sig = Disperser.ar(
                input: sig,
                freq: f0,
                resonance: \resonance.kr(0.75).lag,
                mix: \disperserMix.kr(1).lag,
                feedback: 1
            );

            sig = sig * g_out;
            sig = sig.tanh;
            sig = sig.sanitize;

            verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
            
            verb_damp = LFNoise2.kr(0.2).range(0, 0.7);

            sig = MiVerb.ar(sig, verb_time, \verbMix.kr(0.1).lag, verb_damp, 0.1, mul: 0.5);

            sig = Compander.ar(sig, sig,
                thresh: 1,
                slopeBelow: 1,
                slopeAbove: 1,
                clampTime:  0.01,
                relaxTime:  0.01
            );

            sig = sig * \amp.kr(1).lag;
        });

        Ndef(\ipf)[999] = \pset -> Pbind(
            \dur, 0.01,
            \f0, ~slider.(0).linexp(0,1,30,20000),
            \alphaRate, ~slider.(1),
            \beta, ~slider.(2),
            \g_init, ~slider.(3),
            \modRate, ~slider.(4).linexp(0.001,1,1,1000),
            \modStereo, ~slider.(5).linexp(0.001,1,0.001,1000) - 0.001,
            \amp, ~knob.(0).lag(0.01),
            \verbMix, ~knob.(1).lag(2),
            \disperserMix, ~knob.(2),
            \resonance, ~knob.(3)
        );

        Ndef(\ipf).play([~bus1.channels, ~convolve_A.channels].flatten);

        ~advance.wait;
        
        // IPF multiphonics B
        \ipf_B.postln;

        Ndef(\ipf, {
            var trig, g_state, g_out, g_prev, dg, md;
            var su, safeVal;
            var alpha;
            var op1, op2, phaseShift;
            var sig;
            var verb_time, verb_damp;

            var f0 = \f0.kr(60).lag2;
            var alpha_start = \alphaStart.kr(0.1);
            var alpha_end = \alphaEnd.kr(1);
            var alpha_rate = \alphaRate.kr(1);
            // var alpha_rate = SinOsc.ar(\alphaRate.kr(0.5)).linlin(-1, 1, 0.01, 9);
            var beta = \beta.kr(0.01);
            var modRate = \modRate.kr(10);
            var modStereo=\modStereo.kr(100);
            var index=\index.kr(0.01);

            trig = Impulse.ar(f0);
            g_state = LocalIn.ar(2);
            g_prev = g_state[1];

            alpha = SinOsc
                .ar(alpha_rate)
                .linexp(-1, 1, alpha_start.max(0.001), alpha_end);

            su = beta * exp(g_state[0] - g_prev);
            safeVal = ((g_state[0] - su) / alpha).max(0.00001);
            g_out = g_state[0] - log(safeVal);

            dg = abs(g_out - g_prev);

            md = [modRate - (modStereo * 0.5), modRate + (modStereo * 0.5)];
            phaseShift = dg.abs * md / f0;

            // op1 = SinOsc.ar(f0, phaseShift * 2pi * f0) * g_out * index;
            op1 = VarSaw.ar(f0, 0, phaseShift * 2pi * f0) * g_out * index;

            op2 = SinOsc.ar(f0 * op1);
            // op2 = Blip.ar(f0 * (1 + op1), 5); // bandlimited pulse train
            
            sig = op2;

            sig = Disperser.ar(
                input: sig,
                freq: f0,
                resonance: \resonance.kr(0.75).lag,
                mix: \disperserMix.kr(1).lag,
                feedback: 1
            );

            sig = sig.tanh;
            sig = sig.sanitize;

            LocalOut.ar([g_out, g_state[0]]);

            verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
            verb_damp = LFNoise2.kr(0.2).range(0, 0.7);
            sig = MiVerb.ar(sig, verb_time, \verbMix.kr(0.1).lag, verb_damp, 0.1);
            sig = Compander.ar(sig, sig,
                thresh: 1,
                slopeBelow: 1,
                slopeAbove: 1,
                clampTime:  0.01,
                relaxTime:  0.01
            );
            
            sig = sig * \amp.kr(1).lag;
            sig;
        });

        Ndef(\ipf)[999] = \pset -> Pbind(
            \dur, 0.01,
            \f0, ~slider.(0).linexp(0,1,30,20000).lag(0.1),
            \index, ~slider.(1).linexp(0,1,0.001,1).lag(0.1),
            \alphaRate, ~slider.(2).linexp(0.001,1,0.001,10),
            \alphaStart, ~slider.(3).clip(0.001,1),
            \alphaEnd, ~slider.(4).clip(0.001,1),
            \beta, ~slider.(5).linexp(0,1,0.001,1),
            \modRate, ~slider.(6).linlin(0.001,1,1,1000),
            \modStereo, ~slider.(7).linlin(0.001,1,0.001,1000) - 0.001,
            \amp, ~knob.(0).linlin(0,1,0,1),
            \verbMix, ~knob.(1).lag(0.1),
            \disperserMix, ~knob.(2),
            \resonance, ~knob.(3)
        );

        ~advance.wait;

        // IPF multiphonics C

        \ipf_C.postln;
        
        Ndef(\ipf, {
            var memsize = 8;
            var g_state, g_out, g_prev, dg, md;
            
            var su, safeVal;
            var alpha;
            var op1, op2, phaseShift;
            var sig;
            var verb_time, verb_damp;
            var betas, eps, k;

            var f0         = \f0.kr(50);
            var alpha_start= \alphaStart.kr(0.1);
            var alpha_end  = \alphaEnd.kr(1);
            var alpha_rate = \alphaRate.kr(0.1);
            var beta       = \beta.kr(10);
            // var beta     = SinOsc.ar(\beta.kr(0.5)).linexp(-1, 1, 0.01, 1);
            var modRate    = \modRate.kr(100);
            var modStereo  = \modStereo.kr(10);
            var index      = \index.kr(0.01);
            var tau        = \tau.kr(0.2);
            var g_init     = \g_init.kr(0.3);

            g_state = LocalIn.ar(memsize);

            g_state = Array.fill(memsize, { |i|
                var delayTime = ControlDur.ir + (i / SampleRate.ir);
                if (i == 0) { g_state[i] } { DelayN.ar(g_state[i], delayTime, delayTime)}
            });

            g_prev = g_state[1];

            alpha = LFSaw.ar(alpha_rate).linexp(-1, 1, alpha_start.max(0.001), alpha_end);

            su = 0;
            (g_state.size - 1).do { |i|
                k = exp((i + 1).neg / tau);
                su = su + beta * k * exp(g_state[0] - g_state[i + 1]);
            };

            eps = 1e-6;
            safeVal = ((g_state[0] - su) / alpha).max(eps);
            g_out = g_state[0] - log(safeVal);

            dg = (g_out - g_prev).abs;
            md = [modRate - (modStereo * 0.5), modRate + (modStereo * 0.5)];
            phaseShift = dg * md / f0;

            op1 = SinOsc.ar(f0, phaseShift * 2pi * f0) * g_out * index;
            op2 = SinOsc.ar(f0 * op1);
            sig = op2.sanitize;

            LocalOut.ar([g_out * \fb.kr(0).lag2(0.1)] ++ g_state[0..(g_state.size - 2)]);

            verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
            verb_damp = LFNoise2.kr(0.2).range(0, 0.7);
            sig = MiVerb.ar(sig, verb_time, \verbMix.kr(0.1).lag2(0.1), verb_damp, 0.1);
            sig = Compander.ar(sig, sig,
                thresh: 1,
                slopeBelow: 1,
                slopeAbove: 1,
                clampTime: 0.01,
                relaxTime: 0.01
            );

            sig = sig * \amp.kr(1).lag2(0.1);
            sig;
        });

        Ndef(\ipf)[999] = \pset -> Pbind(
            \dur, 0.01,
            \f0, ~slider.(0).linexp(0,1,30,40000).lag(0.1),
            \index, ~slider.(1).linexp(0,1,0.001,1).lag(0.1),
            \alphaRate, ~slider.(2).linexp(0.001,1,0.001,1000),
            \alphaStart, ~slider.(3).clip(0.001,1),
            \alphaEnd, ~slider.(4).clip(0.001,1),
            \beta, ~slider.(5).linexp(0.001,1,0.001,100),
            \modRate, ~slider.(6).linlin(0.001,1,1,1000),
            \modStereo, ~slider.(7).linexp(0.001,1,0.001,1) - 0.001,
            \amp, ~knob.(0),
            \verbMix, ~knob.(1),
            \tau, ~knob.(2).linexp(0,1,0.001,1),
            \fb, ~knob.(3),
        );

        ~advance.wait;

        // IPF multiphonics D

        \ipf_D.postln;
        \end.postln;

        Ndef(\ipf, {
            var trig, g_state, g_out, g_prev, dg, md;
            var su, safeVal;
            var alpha;
            var op1, op2, phaseShift;
            var sig;
            var verb_time, verb_damp;

            // --- parameters ---
            var f0        = \f0.kr(4000);
            var alpha0    = \alpha0.kr(0.02);
            // var alpha0 = SinOsc.ar(\alpha0.kr(0.5)).linexp(-1, 1, 0.01, 9);
            // var gamma     = \gamma.kr(1);
            var gamma     = SinOsc.ar(\gamma.kr(0.5)).linexp(-1, 1, 0.01, 1);
            var beta      = \beta.kr(0.0001);
            // var beta     = SinOsc.ar(\beta.kr(0.0001)).linexp(-1, 1, 0.0001, 1);
            var modRate   = \modRate.kr(1);
            var modStereo = \modStereo.kr(100);
            var index     = \index.kr(0.005);

            // --- trigger per cycle ---
            trig = Impulse.ar(f0);

            // --- hold [current, previous] g values ---
            g_state = LocalIn.ar(2);
            g_prev = g_state[1];


            // --- state-dependent alpha ---
            alpha = (alpha0 * (1 + (gamma * g_state[0]))).max(1e-4);

            // --- normalized beta recurrence ---
            su = (beta / (1 + beta)) * exp(g_state[0] - g_prev);
            safeVal = ((g_state[0] - su) / alpha).max(1e-6);
            g_out = g_state[0] - log(safeVal);

            // --- phase shift modulation ---
            dg = (g_out - g_prev).abs;
            md = [modRate - (modStereo * 0.5), modRate + (modStereo * 0.5)];
            phaseShift = dg * md / f0;

            // --- oscillators ---
            op1 = SinOsc.ar(f0, phaseShift * 2pi * f0) * g_out * index;
            op2 = SinOsc.ar(f0 * op1);

            sig = op2.tanh.sanitize;

            // --- update state ---
            LocalOut.ar([g_out, g_state[0]]);

            // --- verb & dynamics ---
            verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
            verb_damp = LFNoise2.kr(0.2).range(0, 0.7);
            sig = MiVerb.ar(sig, verb_time, \verbMix.kr(0.1), verb_damp, 0.1);

            sig = Compander.ar(sig, sig,
                thresh: 1,
                slopeBelow: 1,
                slopeAbove: 1,
                clampTime: 0.01,
                relaxTime: 0.01
            );

            sig = sig * \amp.kr(1);
            sig;
        });

        Ndef(\ipf)[999] = \pset -> Pbind(
            \dur, 0.01,
            \f0, ~slider.(0).linexp(0,1,30,20000).lag(2),
            \index, ~slider.(1).linexp(0,1,0.0001,1).lag(2),
            \alpha0, ~slider.(2).linexp(0,1,0.001,1).lag(2),
            \gamma, ~slider.(3).linexp(0,1,0.001,5).lag(2),
            \beta, ~slider.(4).linexp(0.001,1,0.00001,1).lag(2),
            \modRate, ~slider.(5).linlin(0.001,1,0.1,1000),
            \modStereo, ~slider.(6).linlin(0.001,1,0,1) - 0.001,
            \amp, ~knob.(0).linexp(0,1,0.01,1).lag(2),
            \verbMix, ~knob.(1).lag(2)
        )

    }.fork(t);
)

(
    
    Pdef(\specSample).stop;
    Ndef(\ipf).stop(fadeTime: 10);
    Ndef(\morph).stop(fadeTime: 10);
)