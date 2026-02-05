{ Logistic.ar(Line.kr(3.55, 3.6, 5), 1000) }.play


// explore via Mouse
{ Logistic.ar(MouseX.kr(3, 3.99), MouseY.kr(10, 10000, 'exponential'), 0.5, 0.5) }.play


(
{ LatoocarfianN.ar(
    SampleRate.ir/0.25,
    LFNoise2.kr(2, 1.5, 1.5) ! 2,
    LFNoise2.kr(2, 1.5, 1.5) ! 2,
    LFNoise2.kr(2, 0.5, 1.5) ! 2,
    LFNoise2.kr(2, 0.5, 1.5) ! 2,
) * 0.2 }.play(s);
)

// default initial params
{ LatoocarfianN.ar(MouseX.kr(20, SampleRate.ir)) * 0.2 }.play(s);

//rhythmic violence
//48k spectrum looping
{Pan2.ar(0.1*GravityGrid.ar(Impulse.ar(0),MouseX.kr(0.01,10,'exponential')))}.play

(
//IPF multiphonics
{
    | 
        f0 = 60, 
        // alpha = 1,
        beta = 0.3,
        g_init = 0.6, 
        modRate=1, 
        modStereo=100
    |
    var trig, g_prev, g, su, safeVal, g_out, md, freq, sig;
    var verb_time, verb_damp;

    var alpha = SinOsc.ar(0.5).linlin(-1, 1, 0.01, 9);
    // var beta = SinOsc.ar(3).linlin(-1, 1, 0.0, 0.2);

    trig = Impulse.ar(f0);
    g_prev = LocalIn.ar(2);
    g = Select.ar((trig > 0), [K2A.ar(g_init), g_prev]);

    su = beta * exp(g - g_prev);
    safeVal = ((g - su) / alpha).max(0.00001);
    g_out = g - log(safeVal);

    LocalOut.kr(g_out);

    md = [modRate, modRate + modStereo];
    freq = f0 / (1 + g_out * md).max(0.001);

    sig = SinOsc.ar(freq);

    sig = sig * g_out;
    sig = sig.tanh;
    sig = sig.sanitize;

    verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
    
    verb_damp = LFNoise2.kr(0.2).range(0, 0.7);

    MiVerb.ar(sig, verb_time, 0.5, verb_damp, 0.1, mul: 0.5);
    sig = Compander.ar(sig, sig,
        thresh: 1,
        slopeBelow: 1,
        slopeAbove: 1,
        clampTime:  0.01,
        relaxTime:  0.01
    );
}.play;
)

//IPF phase shift
(
{
    | 
        f0 = 2000, 
        alpha_start = 0.1, 
        alpha_end = 1,
        alpha_rate= 0.01,
        beta = 0.01,
        modRate = 10, 
        modStereo = 100,
        index=0.01
    |

    var trig, g_state, g_out, g_prev, dg, md;
    var su, safeVal;
    var alpha;
    var op1, op2, phaseShift;
    var sig;
    var verb_time, verb_damp;
    // var index = SinOsc.ar(0.01).linexp(-1, 1, 0.05, 0.1);
    // var beta = SinOsc.ar(3).linlin(-1, 1, 0.01, 200);

    trig = Impulse.ar(f0);
    g_state = LocalIn.ar(2); // [current g_out, previous g_out]
    g_prev = g_state[1];

    // alpha = XLine.ar(alpha_start, alpha_end, alpha_dur);
    alpha = LFSaw
        .ar(alpha_rate)
        .linexp(-1, 1, alpha_start.max(0.001), alpha_end);

    su = beta * exp(g_state[0] - g_prev);
    safeVal = ((g_state[0] - su) / alpha).max(0.00001);
    g_out = g_state[0] - log(safeVal);

    dg = abs(g_out - g_prev);

    md = [modRate, modRate + modStereo];
    phaseShift = dg.abs * md / f0;

    op1 = SinOsc.ar(f0, phaseShift * 2pi * f0) * g_out * index;
    op2 = SinOsc.ar(f0 * op1);

    sig = op2;
    sig = sig.tanh;
    sig = sig.sanitize;

    LocalOut.ar([g_out, g_state[0]]);

    verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
    verb_damp = LFNoise2.kr(0.2).range(0, 0.7);
    sig = MiVerb.ar(sig, verb_time, 0.1, verb_damp, 0.1);
    sig = Compander.ar(sig, sig,
        thresh: 1,
        slopeBelow: 1,
        slopeAbove: 1,
        clampTime:  0.01,
        relaxTime:  0.01
    );

    sig
    
}.play;
)

//IPF phase shift with Band Limited Impulse train
(
{
    | 
        f0 = 40000, 
        alpha_start = 0.1, 
        alpha_end = 0.5,
        alpha_rate= 1,
        beta = 5,
        modRate = 20, 
        modStereo = 10,
        index=1
    |

    var trig, g_state, g_out, g_prev, dg, md;
    var su, safeVal;
    var alpha;
    var op1, op2, phaseShift;
    var sig;
    var verb_time, verb_damp;
    // var index = SinOsc.ar(0.01).linexp(-1, 1, 0.05, 0.1);
    // var beta = SinOsc.ar(3).linlin(-1, 1, 0.01, 200);

    trig = Impulse.ar(f0);
    g_state = LocalIn.ar(2); // [current g_out, previous g_out]
    g_prev = g_state[1];

    // alpha = XLine.ar(alpha_start, alpha_end, alpha_dur);
    alpha = LFSaw
        .ar(alpha_rate)
        .linexp(-1, 1, alpha_start.max(0.001), alpha_end);

    su = beta * exp(g_state[0] - g_prev);
    safeVal = ((g_state[0] - su) / alpha).max(0.00001);
    g_out = g_state[0] - log(safeVal);

    dg = abs(g_out - g_prev);

    md = [modRate, modRate + modStereo];
    phaseShift = dg.abs * md / f0;
// 
    // op1 = SinOsc.ar(f0, phaseShift * 2pi * f0) * g_out * index;
    
    op1 = VarSaw.ar(f0, 0, phaseShift) * g_out * index;
    op2 = Blip.ar(f0 * (1 + op1), 5); // bandlimited pulse train
    // op2 = SinOsc.ar(f0 * op1);

    sig = op2;
    sig = sig.tanh;
    sig = sig.sanitize;

    LocalOut.ar([g_out, g_state[0]]);

    verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
    verb_damp = LFNoise2.kr(0.2).range(0, 0.7);
    sig = MiVerb.ar(sig, verb_time, 0.1, verb_damp, 0.1);
    sig = Compander.ar(sig, sig,
        thresh: 1,
        slopeBelow: 1,
        slopeAbove: 1,
        clampTime:  0.01,
        relaxTime:  0.01
    );

    sig
    
}.play;
)

//normalized beta term
(
{
    |
        f0 = 4000, 
        alpha_start = 0.1, 
        alpha_end = 0.5,
        alpha_rate = 0.01,
        beta = 0.001,
        modRate = 1, 
        modStereo = 100,
        index = 1
    |

    var trig, g_state, g_out, g_prev, dg, md;
    var su, safeVal;
    var alpha;
    var op1, op2, phaseShift;
    var sig;
    var verb_time, verb_damp;

    // --- trigger per cycle ---
    trig = Impulse.ar(f0);

    // --- hold [current, previous] g values ---
    g_state = LocalIn.ar(2);
    g_prev = g_state[1];

    // --- alpha sweep ---
    alpha = LFSaw.ar(alpha_rate).linexp(-1, 1, alpha_start.max(0.001), alpha_end);

    // --- normalized beta term ---
    su = (beta / (1 + beta)) * exp(g_state[0] - g_prev);

    // --- recurrence ---
    safeVal = ((g_state[0] - su) / alpha).max(1e-6);
    g_out = g_state[0] - log(safeVal);

    // --- phase shift modulation ---
    dg = (g_out - g_prev).abs;
    md = [modRate, modRate + modStereo];
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
    sig = MiVerb.ar(sig, verb_time, 0.1, verb_damp, 0.1);
    sig = Compander.ar(sig, sig,
        thresh: 1,
        slopeBelow: 1,
        slopeAbove: 1,
        clampTime: 0.01,
        relaxTime: 0.01
    );

    sig
}.play;
)


//memory kernel
(
{
    |
        f0 = 50, 
        alpha_start = 0.1, 
        alpha_end = 1,
        alpha_rate = 0.1,
        beta = 10,
        modRate = 100, 
        modStereo = 10,
        index = 0.01,
        tau=0.2
    |

    var trig, g_state, g_out, g_prev, dg, md;
    var su, safeVal;
    var alpha;
    var op1, op2, phaseShift;
    var sig;
    var verb_time, verb_damp;
    var betas, eps, rawsu, k;

    // --- trigger per cycle ---
    trig = Impulse.ar(f0);

    // --- store [g_out, g_prev, g_prev-2, ...] ---
    g_state = LocalIn.ar(8); // make a bigger buffer for memory (8 = current + 7 past values)
    g_prev = g_state[1];

    // --- alpha modulation ---
    alpha = LFSaw.ar(alpha_rate).linexp(-1, 1, alpha_start.max(0.001), alpha_end);

    // --- define betas ---
    betas = Array.fill(g_state.size-1, { beta }); // all betas equal, could customize

    // --- memory kernel recurrence ---
    su = 0;
    (g_state.size-1).do { |i|
        k = exp((i+1).neg / tau);
        su = su + betas[i] * k * exp(g_state[0] - g_state[i+1]);
    };

    eps = 1e-6;
    safeVal = ((g_state[0] - su) / alpha).max(eps);
    g_out = g_state[0] - log(safeVal);

    // --- phase shift ---
    dg = (g_out - g_prev).abs;
    md = [modRate, modRate + modStereo];
    phaseShift = dg * md / f0;

    // --- oscillators ---
    op1 = SinOsc.ar(f0, phaseShift * 2pi * f0) * g_out * index;
    op2 = SinOsc.ar(f0 * op1);

    sig = op2.tanh.sanitize;

    LocalOut.ar([g_out] ++ g_state[0..(g_state.size-2)]);

    // --- verb & dynamics ---
    verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
    verb_damp = LFNoise2.kr(0.2).range(0, 0.7);
    sig = MiVerb.ar(sig, verb_time, 0.1, verb_damp, 0.1);
    sig = Compander.ar(sig, sig,
        thresh: 1,
        slopeBelow: 1,
        slopeAbove: 1,
        clampTime: 0.01,
        relaxTime: 0.01
    );

    sig
}.play;
)

//state dependent alpha
(
{
    |
        f0 = 4000, 
        alpha0 = 0.02,    // base alpha
        gamma = 1,     // scaling factor for state dependence
        beta = 0.0001,
        modRate = 1, 
        modStereo = 100,
        index = 0.005
    |

    var trig, g_state, g_out, g_prev, dg, md;
    var su, safeVal;
    var alpha;
    var op1, op2, phaseShift;
    var sig;
    var verb_time, verb_damp;

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
    md = [modRate, modRate + modStereo];
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
    sig = MiVerb.ar(sig, verb_time, 0.1, verb_damp, 0.1);
    sig = Compander.ar(sig, sig,
        thresh: 1,
        slopeBelow: 1,
        slopeAbove: 1,
        clampTime: 0.01,
        relaxTime: 0.01
    );

    sig
}.play;
)
