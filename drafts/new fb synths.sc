    
(
    ~getTriangle = { |phase, skew|
        phase = phase.linlin(0, 1, skew.neg, 1 - skew);
        phase.bilin(0, skew.neg, 1 - skew, 1, 0, 0);
    };
    
    ~scurve = { |x, curve|
        var v1 = x - (curve * x);
        var v2 = curve - (2 * curve * x.abs) + 1;
        v1 / v2;
    };
    
    ~sigmoidBipolar = { |x, shape|
        var shapeBipolar = shape * 2 - 1;
        var xBipolar = x * 2 - 1;
        ~scurve.(xBipolar, shapeBipolar) * 0.5 + 0.5;
    };
    
    ~sigmoidUnipolar = { |x, shape|
        var shapeBipolar = shape * 2 - 1;
        ~scurve.(x, shapeBipolar);
    };
    
    ~sigmoidBlended = { |x, shape, mix|
        var unipolar = ~sigmoidUnipolar.(x, shape);
        var bipolar = ~sigmoidBipolar.(x, shape);
        unipolar * (1 - mix) + (bipolar * mix);
    };

    ~getTrapezoid = { |phase, duty, shape|
        var offset = phase - (1 - duty);
        var steepness = 1 / (1 - shape);
        var trapezoid = (offset * steepness + (1 - duty)).clip(0, 1);
        var pulse = offset > 0;
        Select.ar(shape |==| 1, [trapezoid, pulse]);
    };
)

(
    SynthDef(\trapezoidFB, {
        var sig, impulse, fb, modEnv1, del2;
        var phase, triangle;
        var time = \freq.kr(50) - ControlRate.ir.reciprocal;
        var damp = (1 - \damp.kr(0.01));
        var damp_env;
        fb = LocalIn.ar(2) * \feedback.kr(0.9);
        fb = Rotate2.ar(fb[0], fb[1], LFNoise2.ar(0.25) * \fbmod.kr(0));
        
        phase = Phasor.ar(0, time / SampleRate.ir);
        triangle = ~getTriangle.(phase, (\skew.ar(1) * 0.01 * (fb)).wrap(-1,1));
        sig = ~getTrapezoid.(triangle, \shape.kr(0), (\duty.ar(0) * 0.01 * (fb)).wrap(-1,1));
        sig = sig.linlin(0, 1, -1, 1);

        sig = AllpassC.ar(sig, 2, 1/time, \ap_fb.ar(0) * (1-fb));

        damp_env = EnvGen.ar(Env.perc(\atk.kr(0.01), \dec.kr(1), curve: -8), \gate.kr(1)) * \dampScale.ar(0);
        sig = OnePole.ar(sig, (damp + damp_env).clip(-1,1));
        sig = sig.sanitize;
        LocalOut.ar(sig);

        sig = SelectX.ar(\drywet.kr(0.5), [
            sig, 
            GVerb.ar(sig, roomsize: \roomsize.kr(10), revtime: 1, damping: 0.5, inputbw: 0.5, spread: 15, drylevel: 1)
        ]);

        Compander.ar(sig, sig,
            thresh: 0.1,
            slopeBelow: 0.1,
            slopeAbove: 1,
            clampTime:  0.01,
            relaxTime:  0.01
        ) * 0.1;
        // sig = HPF.ar(sig, \hpf.kr(30));
        sig = sig * \gain.kr(0).dbamp;
        sig = sig * \amp.kr(0.4);
        Out.ar(\out.kr(0), sig);
    }).add;
)

(
    SynthDef(\sigmoidFB, {
        var sig, impulse, fb, modEnv1, del2;
        var phase, triangle;
        var time = \freq.kr(50) - ControlRate.ir.reciprocal;
        var damp = (1 - \damp.kr(0.01));
        var damp_env;
        fb = LocalIn.ar(2) * \feedback.kr(0.9);
        fb = Rotate2.ar(fb[0], fb[1], LFNoise2.ar(0.25) * \fbmod.kr(0));
        
        phase = Phasor.ar(0, time / SampleRate.ir);
        triangle = ~getTriangle.(phase, (\skew.ar(1) * 0.01 * (fb)).wrap(-1,1));
        sig = ~sigmoidUnipolar.(triangle, (\curve.ar(0) * 0.01 * (fb)).wrap(-1,1));
        sig = sig.linlin(0, 1, -1, 1);

        sig = AllpassC.ar(sig, 2, 1/time, \ap_fb.ar(0) * (1-fb));
        damp_env = EnvGen.ar(Env.perc(\atk.kr(0.01), \dec.kr(1), curve: -8), \gate.kr(1)) * \dampScale.ar(0);
        sig = OnePole.ar(sig, (damp));
        sig = sig.sanitize;
        LocalOut.ar(sig);

        sig = SelectX.ar(\drywet.kr(0.5), [
            sig, 
            GVerb.ar(sig, roomsize: \roomsize.kr(10), revtime: 1, damping: 0.5, inputbw: 0.5, spread: 15, drylevel: 1)
        ]);

        Compander.ar(sig, sig,
            thresh: 0.1,
            slopeBelow: 0.1,
            slopeAbove: 1,
            clampTime:  0.01,
            relaxTime:  0.01
        ) * 0.1;
        // sig = HPF.ar(sig, \hpf.kr(30));
        sig = sig * \gain.kr(0).dbamp;
        sig = sig * \amp.kr(0.4);
        Out.ar(\out.kr(0), sig);
    }).add;
)

x.free()

(
x = Synth(\trapezoidFB, [
    freq: 30, 
    damp: 0.3, 
    feedback: 1, 
    ap_fb: 1, 
    shape: 0.5,
    duty: 0.4,  
    fbmod: 1,
    drywet: 0.1,
    atk: 0.01,
    dec: 0.5,
    dampScale: 0.01,
    gain: -20
    // hpf: 30
])
)

(
x = Synth(\sigmoidFB, [
    freq: 30, 
    damp: 0.1, 
    feedback: -1, 
    ap_fb: 1, 
    skew: 0.5,
    curve: 0.4,  
    fbmod: 1,
    drywet: 1,
    atk: 0.01,
    dec: 0.5,
    dampScale: 0.01,
    gain: -20
    // hpf: 30
])
)

(
    Pmono(\sigmoidFB,
        \freq, 440,
        \damp, ~pmodenv.(Pseq([0.01, 0.1], inf), 8, inf, \sine),
        \feedback, ~pmodenv.(Prand([-1, 1], inf), 2, inf, \sine),
        \ap_fb, ~pmodenv.(Pseq([-1, 1], inf), 1.5, inf, \sine),
        \curve, ~pmodenv.(Pseq([0, 1], inf), 3, inf, \sine),
        \skew, ~pmodenv.(Prand([0.4, 1], inf), 6, inf, \linear),
        \fbmod, 1,
        \drywet, 1,
        \atk, 0.01,
        \dec, 0.5,
        \dampScale, 0.01,
        \gain, -20,
        \amp, 1,
    ).play(t)
)

(
    Pmono(\sigmoidFB,
        \freq, 440,
        \damp, ~pmodenv.(Pseq([0.01, 0.5], inf), 8, inf, \sine),
        \feedback, ~pmodenv.(Prand([-1, 1], inf), 2, inf, \sine),
        \ap_fb, ~pmodenv.(Pseq([-1, 1], inf), 1.5, inf, \sine),
        \duty, ~pmodenv.(Pseq([0.1, 0.5], inf), 3, inf, \sine),
        \skew, ~pmodenv.(Prand([0.4, 1], inf), 6, inf, \linear),
        \shape, ~pmodenv.(Prand([0, 1], inf), 6, inf, \linear),
        \fbmod, 1,
        \drywet, 1,
        \atk, 0.01,
        \dec, 0.5,
        \dampScale, 0.01,
        \gain, -20,
        \amp, 1,
    ).play(t)
)