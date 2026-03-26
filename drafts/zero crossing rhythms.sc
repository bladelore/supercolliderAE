(
    {
        a = Impulse.ar(Changed.ar(SinOsc.ar(1000).sign));
        b = Impulse.ar(Changed.ar(SinOsc.ar(5000).sign));
        // c = a & b;
    }.play
)

ZeroCrossing

(
{
    var sig1 = SinOsc.ar(2);
    var sig2 = SinOsc.ar(4);

    var z1 = HPZ1.ar(sig1).sign.abs;  // pulse at zero crossing
    var z2 = HPZ1.ar(sig2).sign.abs;

    var both = z1 * z2; // logical AND (coincidence)

    // both * 0.2
}.play
)

(
{
    var a, b, c;

    a = Changed.ar(SinOsc.ar(2).sign);
    b = Changed.ar(SinOsc.ar(4).sign);

    c = a * b;  // AND
    // b
    // a
    // c.scope

    // Decay2.ar(c, 0.001, 0.02) * 0.4
}.play
)

(
{
    var p1 = Phasor.ar(0, 7/SampleRate.ir, 0, 1);
    var p2 = Phasor.ar(0, 5/SampleRate.ir, 0, 1);

    var z1 = (p1 <= 0.001);
    var z2 = (p2 <= 0.001);


    var both = [z1, z2];

    Decay2.ar(both, 0.04, 0.003)
}.play
)

(
var t60, riseScale, riseFac, normFac;

// Approximate 60 dB decay time
// (actual t60 will increase with riseScale)
t60 = 0.01;

// Scale the rise time (< 1.0)
riseScale = 0.1;

// Calculate normalization factor
riseFac = riseScale / (2 - riseScale);
normFac = riseFac.pow(riseFac / (riseFac - 1)) / (1 - riseFac);

// Plot and compare to Decay
plot({
    var imp = Impulse.ar(0);
    [
        Decay.ar(imp, t60),
        Decay2.ar(imp,
            attackTime: t60 * riseFac,
            decayTime: t60,
            mul: normFac
        )
    ]
}, duration: t60)
.plotColor_([Color.blue, Color.red])
.superpose_(true)
);

(
Ndef(\a, {
    var dur = 10;
    var fc  = 110;
    var fm  = 110;
    var t   = Sweep.ar(1, 1).min(dur);

    var a     = 0.9 * (1 - (t / dur));
    var theta = 2pi * fc * t;
    var beta  = 2pi * fm * t;

    var denom = (1 + a.squared) - (2 * a * cos(beta));
    var numer = sin(theta) - (a * sin(theta - beta));

    var sig = numer / (denom + 1e-10);

    sig = sig * 0.2;

    sig ! 2
}).play
)

(
Ndef(\a, {
    var dur = 10;
    var fc  = 110;
    var fm  = 110;
    var t   = Sweep.ar(1, 1).min(dur);

    // max a to keep partials below Nyquist
    var nyquist    = SampleRate.ir / 2;
    var maxPartials = (nyquist / fm).floor;
    var aMax       = 2.pow(-1 / maxPartials) * 0.999;

    var a     = aMax * (1 - (t / dur));
    var theta = 2pi * fc * t;
    var beta  = 2pi * fm * t;

    var denom = (1 + a.squared) - (2 * a * cos(beta));
    var numer = sin(theta) - (a * sin(theta - beta));

    var sig = numer / (denom + 1e-10);
    sig = sig * 0.2;
    sig ! 2
}).play
)

//GOOD ONE
(
Ndef(\a, {
    var dur = 10;
    var fc  = 200;
    var fm  = [40, 42] * 8;
    // var t   = Sweep.ar(1, 1).min(dur);
    var t = Phasor.ar(0, SampleDur.ir / 10) * 1;

    var a = (1 - Phasor.ar(0, SampleDur.ir * 0.1)) * 0.9;
    var beta  = 2pi * fm * t;

    var theta = Phasor.ar(0, fc / SampleRate.ir * 2pi, 0, 2pi);

    var denom = (1 + a.squared) - (2 * a * cos(beta));
    var numer = (1 - a.squared) * sin(theta - beta);

    var sig = numer / (denom + 1e-10);

    t.poll;

    sig = sig * 0.1;

    Splay.ar(sig, 1);
}).play
)

(
Ndef(\a, {
    var dur = 10;
    var fc  = 110;
    var fm  = 5;
    var t   = Sweep.ar(1, 1).min(dur);

    // max a to keep partials below Nyquist
    var nyquist     = SampleRate.ir / 2;
    var maxPartials = (nyquist / fm).floor;
    var aMax        = 2.pow(-1 / maxPartials) * 0.999;

    var a     = aMax * (1 - (t / dur));
    var beta  = 2pi * fm * t;
    var theta = Phasor.ar(0, fc / SampleRate.ir * 2pi, 0, 2pi);

    var denom = (1 + a.squared) - (2 * a * cos(beta));
    var numer = (1 - a.squared) * sin(theta - beta);

    var sig = numer / (denom + 1e-10);
    sig.scope;
    sig ! 2
}).play
)

(
// Upper sidebands only
Ndef(\dsf_upper, {
    var dur = 10;
    var fc  = 110;
    var fm  = 110;
    var t   = Sweep.ar(1, 1).min(dur);
    var a   = 0.9 * (1 - (t / dur));
    var theta = Phasor.ar(0, fc / SampleRate.ir * 2pi, 0, 2pi);
    var beta  = 2pi * fm * t;

    var sig = (sin(theta) - (a * sin(theta - beta)))
              / ((1 + a.squared) - (2 * a * cos(beta)) + 1e-10);
    sig ! 2
}).play
)

(
// Double sidebands
Ndef(\dsf_double, {
    var dur = 10;
    var fc  = 110;
    var fm  = 55;
    var t   = Sweep.ar(1, 1).min(dur);
    var a   = 0.9 * (1 - (t / dur));
    var theta = Phasor.ar(0, fc / SampleRate.ir * 2pi, 0, 2pi);
    var beta  = 2pi * fm * t;

    var sig = (1 - a.squared) * sin(theta - beta)
              / ((1 + a.squared) - (2 * a * cos(beta)) + 1e-10);
    sig ! 2
}).play
)

(
// Cosine version - same spectrum, 90 degree phase shift
Ndef(\dsf_cosine, {
    var dur = 10;
    var fc  = 110;
    var fm  = 55;
    var t   = Sweep.ar(1, 1).min(dur);
    var a   = 0.9 * (1 - (t / dur));
    var theta = Phasor.ar(0, fc / SampleRate.ir * 2pi, 0, 2pi);
    var beta  = 2pi * fm * t;

    var sig = (cos(theta) - (a * cos(theta - beta)))
              / ((1 + a.squared) - (2 * a * cos(beta)) + 1e-10);
    sig ! 2
}).play
)

(
// Inharmonic - fc and fm at different ratios, classic bell/metal
Ndef(\dsf_inharmonic, {
    var dur = 10;
    var fc  = 110;
    var fm  = 137;  // irrational ratio -> inharmonic partials
    var t   = Sweep.ar(1, 1).min(dur);
    var a   = 0.9 * (1 - (t / dur));
    var theta = Phasor.ar(0, fc / SampleRate.ir * 2pi, 0, 2pi);
    var beta  = 2pi * fm * t;

    var sig = (sin(theta) - (a * sin(theta - beta)))
              / ((1 + a.squared) - (2 * a * cos(beta)) + 1e-10);
    sig ! 2
}).play
)

(
// Attack/decay envelope on a - bright attack, decays to sine
Ndef(\dsf_attdec, {
    var dur = 10;
    var fc  = 110;
    var fm  = 110;
    var t   = Sweep.ar(1, 1).min(dur);
    var a   = 0.9 * (1 - ((t - (dur/2)).abs / (dur/2)));  // triangle envelope
    var theta = Phasor.ar(0, fc / SampleRate.ir * 2pi, 0, 2pi);
    var beta  = 2pi * fm * t;

    var sig = (sin(theta) - (a * sin(theta - beta)))
              / ((1 + a.squared) - (2 * a * cos(beta)) + 1e-10);
    sig ! 2
}).play
)

(
// Modulate 'a' with a slow LFO - evolving timbre
Ndef(\dsf_lfo, {
    var fc  = 110;
    var fm  = 110;
    var t   = Sweep.ar(1, 1);
    var a   = SinOsc.kr(0.2).range(0.01, 0.95);
    var theta = Phasor.ar(0, fc / SampleRate.ir * 2pi, 0, 2pi);
    var beta  = 2pi * fm * t;

    var sig = (sin(theta) - (a * sin(theta - beta)))
              / ((1 + a.squared) - (2 * a * cos(beta)) + 1e-10);
    sig ! 2
}).play
)

(
// Sweep fm for a gliding harmonic series
Ndef(\dsf_fmsweep, {
    var fc  = 110;
    var fm  = Line.kr(55, 220, 10);  // sweep partial spacing
    var t   = Sweep.ar(1, 1);
    var a   = 0.7;
    var theta = Phasor.ar(0, fc / SampleRate.ir * 2pi, 0, 2pi);
    var beta  = 2pi * fm * t;

    var sig = (sin(theta) - (a * sin(theta - beta)))
              / ((1 + a.squared) - (2 * a * cos(beta)) + 1e-10);
    sig ! 2
}).play
)

// ============================================================
// CLICKS & CUTS PERCUSSION SYNTHDEFS
// ============================================================
// Boot the server first: s.boot;

(
// -------------------------------------------------------
// 1. BASIC CLICK — bare transient impulse
// -------------------------------------------------------
SynthDef(\click, { |out=0, amp=0.8, pan=0, atk=0.0001, rel=0.01, freq=8000|
    var sig, env;
    env = EnvGen.kr(Env.perc(atk, rel, 1, -8), doneAction: 2);
    sig = WhiteNoise.ar * env;
    sig = HPF.ar(sig, freq);
    sig = Pan2.ar(sig, pan, amp);
    Out.ar(out, sig);
}).add;

// -------------------------------------------------------
// 2. DIGITAL CLICK — hard-clipped sine burst, very digital
// -------------------------------------------------------
SynthDef(\digitalClick, { |out=0, amp=0.9, pan=0, freq=1200, rel=0.008|
    var sig, env;
    env = EnvGen.kr(Env.perc(0.0001, rel, 1, -12), doneAction: 2);
    sig = SinOsc.ar(freq * [1, 1.5, 3]) * env;
    sig = (sig * 8).tanh; // hard clip / saturation
    sig = Mix(sig) * 0.4;
    sig = HPF.ar(sig, 800);
    sig = Pan2.ar(sig, pan, amp);
    Out.ar(out, sig);
}).add;

// -------------------------------------------------------
// 3. GLITCH CUT — stuttered noise burst, lo-fi feel
// -------------------------------------------------------
SynthDef(\glitchCut, { |out=0, amp=0.7, pan=0, bits=6, rate=8000, rel=0.04|
    var sig, env;
    env = EnvGen.kr(Env.perc(0.001, rel, 1, -4), doneAction: 2);
    sig = WhiteNoise.ar;
    sig = Decimator.ar(sig, rate, bits); // bit crush
    sig = sig * env;
    sig = BPF.ar(sig, LFNoise1.kr(30).exprange(400, 6000), 0.8);
    sig = Pan2.ar(sig, pan, amp);
    Out.ar(out, sig);
}).add;

// -------------------------------------------------------
// 4. TICK — tight pitched transient, like a rimshot or hi-hat
// -------------------------------------------------------
SynthDef(\tick, { |out=0, amp=0.8, pan=0, freq=4000, tone=0.3, rel=0.02|
    var noise, sine, sig, env;
    env = EnvGen.kr(Env.perc(0.0001, rel, 1, -10), doneAction: 2);
    noise = HPF.ar(WhiteNoise.ar, 3000);
    sine  = SinOsc.ar(freq) * 0.5;
    sig   = XFade2.ar(noise, sine, tone * 2 - 1); // blend noise <-> tone
    sig   = sig * env;
    sig   = Pan2.ar(sig, pan, amp);
    Out.ar(out, sig);
}).add;

// -------------------------------------------------------
// 5. SCRAPE — pitched scratchy texture burst
// -------------------------------------------------------
SynthDef(\scrape, { |out=0, amp=0.6, pan=0, freq=200, rel=0.06|
    var sig, env;
    env = EnvGen.kr(Env.perc(0.001, rel, 1, -3), doneAction: 2);
    sig = Saw.ar(freq * LFNoise2.ar(80).exprange(0.5, 2.0));
    sig = sig + PinkNoise.ar(0.5);
    sig = (sig * 4).fold2(1); // wave folding for grit
    sig = BPF.ar(sig, freq * 4, 2.0) * env;
    sig = Pan2.ar(sig, pan, amp);
    Out.ar(out, sig);
}).add;

// -------------------------------------------------------
// 6. CRUNCH — low-end transient with distorted body
// -------------------------------------------------------
SynthDef(\crunch, { |out=0, amp=0.8, pan=0, freq=80, rel=0.05, drive=6|
    var sig, env, body, click;
    env   = EnvGen.kr(Env.perc(0.001, rel, 1, -6), doneAction: 2);
    click = HPF.ar(WhiteNoise.ar, 2000) * EnvGen.kr(Env.perc(0.0001, 0.005));
    body  = SinOsc.ar(freq * XLine.kr(4, 1, rel)); // pitch drop
    sig   = (body + click) * env;
    sig   = (sig * drive).tanh * (1/drive.sqrt); // soft clip
    sig   = Pan2.ar(sig, pan, amp);
    Out.ar(out, sig);
}).add;

// -------------------------------------------------------
// 7. STUTTER CUT — rapid amplitude chop (gating artifact)
// -------------------------------------------------------
SynthDef(\stutterCut, { |out=0, amp=0.7, pan=0, rate=32, rel=0.12|
    var sig, env, gate;
    env  = EnvGen.kr(Env.perc(0.001, rel, 1, -2), doneAction: 2);
    sig  = WhiteNoise.ar + Impulse.ar(200);
    sig  = HPF.ar(sig, 1000);
    gate = LFPulse.ar(rate, 0, 0.5); // rapid chop
    sig  = sig * gate * env;
    sig  = Pan2.ar(sig, pan, amp);
    Out.ar(out, sig);
}).add;

// -------------------------------------------------------
// 8. ZAP CLICK — electronic zap with fast pitch sweep
// -------------------------------------------------------
SynthDef(\zapClick, { |out=0, amp=0.8, pan=0, startFreq=8000, endFreq=100, rel=0.05|
    var sig, env, freq;
    env  = EnvGen.kr(Env.perc(0.0001, rel, 1, -8), doneAction: 2);
    freq = XLine.kr(startFreq, endFreq, rel);
    sig  = SinOsc.ar(freq) + (WhiteNoise.ar * 0.3);
    sig  = (sig * 3).tanh;
    sig  = sig * env;
    sig  = Pan2.ar(sig, pan, amp);
    Out.ar(out, sig);
}).add;

"SynthDefs loaded! Ready to play.".postln;
)

// ============================================================
// USAGE EXAMPLES
// ============================================================

// Single hits:
Synth(\click)
Synth(\digitalClick)
Synth(\glitchCut)
Synth(\tick,       [\freq, 6000, \tone, 0.1])
Synth(\scrape,     [\freq, 150,  \rel, 0.1])
Synth(\crunch,     [\drive, 10])
Synth(\stutterCut, [\rate, 48])
Synth(\zapClick,   [\startFreq, 12000, \endFreq, 50])

// Rhythmic pattern using Pbind:
(
Pbind(
    \instrument, Pwrand(
        [\click, \digitalClick, \glitchCut, \tick, \zapClick, \crunch, \scrape, \stutterCut],
        [0.1,    0.15,          0.15,       0.15,  0,       0.1,     0.1,     0.05], inf
    ),
    \dur,  0.25,
    \amp,  Pwhite(0.4, 0.9),
    \pan,  Pwhite(-0.8, 0.8),
    \rel,  Pwhite(0.005, 0.2),
    \freq, Pwhite(100, 8),
    // \atk, 0.1,
).play(t)
)

t.tempo*60

(
// ============================================================
// FM PERCUSSION VARIATIONS — based on fmPerc3
// ============================================================
// Each variation mutates a specific aspect of the original:
// topology, modulator waveforms, feedback routing, envelope
// character, spectral shaping, and stereo/spatial treatment.
// ============================================================


// -------------------------------------------------------
// VARIATION 1: fmPercMetal
// Stacked FM with frequency-ratio spreading for metallic
// inharmonic tones. Two modulators detune against each other
// creating beating partials. Good for cymbals, cowbells, rims.
// -------------------------------------------------------
SynthDef(\fmPercMetal, {|gate=1|
    var sig, freq, pitchEnv, atk, dec, sweep, fb, ratio1, ratio2,
        drive, index1, index2, detune, noise, fbIn, car, mod1, mod2, mod3;

    freq   = \freq.kr(440);
    atk    = \atk.kr(0.001);
    dec    = \dec.kr(0.3);
    fb     = \fb.kr(0.5);
    index1 = \index1.kr(3);
    index2 = \index2.kr(2);
    ratio1 = \ratio1.kr(1.41);   // inharmonic — sqrt(2)
    ratio2 = \ratio2.kr(2.73);   // inharmonic
    drive  = \drive.kr(6);
    sweep  = \sweep.kr(12);
    detune = 2**(\spread.kr(15) / 1200);
    noise  = \noise.kr(0.3);

    fbIn = LocalIn.ar(2) * \feedback.kr(0.6);
    fbIn = Rotate2.ar(fbIn[0], fbIn[1], LFNoise2.ar(0.5) * \fbmod.kr(0.3));

    pitchEnv = (1 + (sweep * Env.perc(0.0, 0.05, curve: -8).ar))
             * XLine.ar(1, 0.5, sweep.reciprocal);

    // third modulator adds shimmer
    mod3 = SinOsc.ar([freq, freq * detune] * pitchEnv * ratio1 * 1.5) * index1 * 0.5;
    mod1 = SinOsc.ar([freq, freq * detune] * pitchEnv * ratio1 + mod3) * index1;
    mod2 = SinOsc.ar([freq, freq * detune] * pitchEnv * ratio2) * index2;

    car = SinOscFB.ar(
        [freq, freq * detune] * pitchEnv * (1 + mod1 + mod2) + fbIn,
        fb
    ) * EnvGen.kr(Env.perc(atk, dec, 1, -6));

    sig = car + (WhiteNoise.ar * XLine.ar(1, 0.01, dec * 0.1) * noise
              * EnvGen.kr(Env.perc(atk, dec * 0.1, 1, -8), gate));

    sig = BHiShelf.ar(sig, \hifreq.kr(5000), 1, \hidb.kr(6));
    sig = BPeakEQ.ar(sig, freq * ratio1, 0.5, \middb.kr(4));

    // metallic ring emphasis
    sig = sig + (Ringz.ar(sig, freq * ratio1, 0.2) * -18.dbamp)
              + (Ringz.ar(sig, freq * ratio2, 0.15) * -20.dbamp);

    sig = (sig * drive.dbamp).tanh * drive.neg.dbamp;

    sig = Compander.ar(sig, sig, thresh: 0.6, slopeAbove: 0.4,
        clampTime: 0.001, relaxTime: 0.5);

    sig = HPF.ar(sig, \hpf.kr(200));
    sig = sig * \gain.kr(-18).dbamp;
    sig = Pan2.ar(sig, \pan.kr(0));
    sig = LeakDC.ar(sig, 0.995);
    sig = sig * \amp.kr(1);

    LocalOut.ar(sig + car + mod1 + mod2);
    DetectSilence.ar(sig, doneAction: 2);
    Out.ar(\out.kr(0), sig);
}).add;


// -------------------------------------------------------
// VARIATION 2: fmPercWood
// Short attack, fast-decaying resonant body. Modulator uses
// triangle waves for a softer, more hollow timbre. Works well
// for woodblocks, claves, small drums. Minimal noise.
// -------------------------------------------------------
SynthDef(\fmPercWood, {|gate=1|
    var sig, freq, pitchEnv, atk, dec, sweep, fb,
        index1, index2, detune, fbIn, car, mod1, mod2;

    freq   = \freq.kr(440);
    atk    = \atk.kr(0.001);
    dec    = \dec.kr(0.08);
    fb     = \fb.kr(0.1);
    index1 = \index1.kr(2);
    index2 = \index2.kr(0.8);
    sweep  = \sweep.kr(6);
    detune = 2**(\spread.kr(5) / 1200); // tight spread

    fbIn = LocalIn.ar(2) * \feedback.kr(0.2);

    pitchEnv = (1 + (sweep * Env.perc(0.0, 0.03, curve: -10).ar))
             * XLine.ar(1, 0.7, sweep.reciprocal);

    // LFTri gives warmer, hollow tone vs Pulse
    mod1 = LFTri.ar([freq, freq * detune] * pitchEnv * \ratio1.kr(2)) * index1;
    mod2 = LFTri.ar([freq, freq * detune] * pitchEnv * \ratio2.kr(3)) * index2;

    car = SinOscFB.ar(
        [freq, freq * detune] * pitchEnv * (1 + mod1 + (mod2 * 0.5)) + fbIn,
        fb
    ) * EnvGen.kr(Env.perc(atk, dec, 1, -8));

    // small body resonance
    sig = car + (Ringz.ar(car, freq * 1.5, 0.05) * -12.dbamp);

    sig = BLowShelf.ar(sig, 300, 1, \lodb.kr(-6));
    sig = BHiShelf.ar(sig, 4000, 1, \hidb.kr(-3));

    sig = Compander.ar(sig, sig, thresh: 0.7, slopeAbove: 0.5,
        clampTime: 0.001, relaxTime: 0.2);

    sig = HPF.ar(sig, \hpf.kr(100));
    sig = sig * \gain.kr(-16).dbamp;
    sig = Pan2.ar(sig, \pan.kr(0));
    sig = LeakDC.ar(sig, 0.995);
    sig = sig * \amp.kr(1);

    LocalOut.ar(sig + car + mod1 + mod2);
    DetectSilence.ar(sig, doneAction: 2);
    Out.ar(\out.kr(0), sig);
}).add;


// -------------------------------------------------------
// VARIATION 3: fmPercChaos
// Feedback-dominant design. The fbIn feeds back through a
// nonlinear waveshaper before re-entering, creating chaotic
// instability. High feedback settings produce unpredictable
// pitched noise bursts. Great for glitchy snares and FX.
// -------------------------------------------------------
SynthDef(\fmPercChaos, {|gate=1|
    var sig, freq, pitchEnv, atk, dec, sweep, fb,
        index1, index2, detune, noise, fbIn, fbWarp,
        car, mod1, mod2;

    freq   = \freq.kr(220);
    atk    = \atk.kr(0.005);
    dec    = \dec.kr(0.4);
    fb     = \fb.kr(2.5);        // high FB for chaos
    index1 = \index1.kr(4);
    index2 = \index2.kr(3);
    sweep  = \sweep.kr(5);
    detune = 2**(\spread.kr(30) / 1200);
    noise  = \noise.kr(0.5);

    fbIn = LocalIn.ar(2) * \feedback.kr(1.5);

    // nonlinear feedback warp — folds the signal before reinsertion
    fbWarp = (fbIn * \fbdrive.kr(3)).fold2(1);
    fbWarp = Rotate2.ar(fbWarp[0], fbWarp[1],
        SinOsc.kr(LFNoise2.kr(2).exprange(0.1, 8)) * \fbmod.kr(0.5));

    pitchEnv = (1 + (sweep * Env.perc(0.0, 0.15, curve: -3).ar))
             * XLine.ar(1, 0.3, sweep.reciprocal);

    mod1 = PulseDPW.ar([freq, freq * detune] * pitchEnv * \ratio1.kr(1.5),
        \pulseWidth.kr(0.3)) * index1;
    mod2 = Saw.ar([freq, freq * detune] * pitchEnv * \ratio2.kr(0.5)) * index2;

    car = SinOscFB.ar(
        [freq, freq * detune] * pitchEnv * (1 + mod1 + mod2) + fbWarp,
        fb
    ) * EnvGen.kr(Env.perc(atk, dec, 1, -4));

    sig = car + (BrownNoise.ar * XLine.ar(1, 0.01, dec * 0.5) * noise
              * EnvGen.kr(Env.perc(atk, dec * 0.3, 1, 2), gate));

    // mild soft clip to keep it from blowing up
    sig = (sig * 0.7).tanh;

    sig = BPeakEQ.ar(sig, \midfreq.kr(800), 0.7, \middb.kr(6));
    sig = BHiShelf.ar(sig, 6000, 1, \hidb.kr(-6));

    sig = sig + (GVerb.ar(sig.sum, \roomsize.kr(5), \reverbtime.kr(2),
        spread: 8) * -20.dbamp);

    sig = Compander.ar(sig, sig, thresh: 0.5, slopeAbove: 0.4,
        clampTime: 0.005, relaxTime: 0.8);

    sig = HPF.ar(sig, \hpf.kr(60));
    sig = sig * \gain.kr(-22).dbamp;
    sig = Pan2.ar(sig, \pan.kr(0));
    sig = LeakDC.ar(sig, 0.995);
    sig = sig * \amp.kr(1);

    LocalOut.ar(sig + car + mod1 + mod2);
    DetectSilence.ar(sig, doneAction: 2);
    Out.ar(\out.kr(0), sig);
}).add;


// -------------------------------------------------------
// VARIATION 4: fmPercThump
// Kick-focused: deep sub-dominant pitch sweep, slow release,
// saturated low mids. Modulator ratios tuned sub-harmonic.
// Heavy compander pumping for punchy dynamic feel.
// -------------------------------------------------------
SynthDef(\fmPercThump, {|gate=1|
    var sig, freq, pitchEnv, atk, dec, sweep, fb,
        index1, index2, detune, noise, fbIn, car, mod1, mod2;

    freq   = \freq.kr(60);
    atk    = \atk.kr(0.003);
    dec    = \dec.kr(0.6);
    fb     = \fb.kr(0.3);
    index1 = \index1.kr(1.5);
    index2 = \index2.kr(0.5);
    sweep  = \sweep.kr(16);      // aggressive pitch sweep
    detune = 2**(\spread.kr(8) / 1200);
    noise  = \noise.kr(0.2);

    fbIn = LocalIn.ar(2) * \feedback.kr(0.3);

    pitchEnv = (1 + (sweep * Env.perc(0.0, 0.08, curve: -6).ar))
             * XLine.ar(1, 0.1, sweep.reciprocal);  // sweeps very low

    mod1 = SinOsc.ar([freq, freq * detune] * pitchEnv * \ratio1.kr(0.5)) * index1;
    mod2 = SinOsc.ar([freq, freq * detune] * pitchEnv * \ratio2.kr(1)) * index2;

    car = SinOscFB.ar(
        [freq, freq * detune] * pitchEnv * (1 + mod1 + mod2) + fbIn,
        fb
    ) * EnvGen.kr(Env.perc(atk, dec, 1, -5));

    sig = car + (PinkNoise.ar * XLine.ar(1, 0.001, dec * 0.05) * noise
              * EnvGen.kr(Env.perc(atk, 0.02, 1, -12), gate));

    sig = BLowShelf.ar(sig, \lofreq.kr(120), 0.7, \lodb.kr(8));
    sig = BPeakEQ.ar(sig, \midfreq.kr(200), 1, \middb.kr(4));
    sig = BHiShelf.ar(sig, 3000, 1, \hidb.kr(-8)); // roll off top

    // soft saturation on low mids
    sig = SelectX.ar(\drivemix.kr(0.3), [sig,
        (sig * \drive.kr(12).dbamp).tanh * \drive.kr(12).neg.dbamp * 1.5]);

    sig = Compander.ar(sig, sig, thresh: 0.3,
        slopeAbove: 0.3, clampTime: 0.002, relaxTime: 1.5);

    sig = HPF.ar(sig, \hpf.kr(20));
    sig = LPF.ar(sig, 8000); // sub focus
    sig = sig * \gain.kr(-14).dbamp;
    sig = Pan2.ar(sig, \pan.kr(0));
    sig = LeakDC.ar(sig, 0.995);
    sig = sig * \amp.kr(1);

    LocalOut.ar(sig + car + mod1 + mod2);
    DetectSilence.ar(sig, doneAction: 2);
    Out.ar(\out.kr(0), sig);
}).add;


// -------------------------------------------------------
// VARIATION 5: fmPercAir
// Airy, open, long decay. Modulator uses sine with slow
// LFO index modulation, creating breathing spectral movement.
// GVerb room is prominent for spatial character. Snare or
// open hat territory.
// -------------------------------------------------------
SynthDef(\fmPercAir, {|gate=1|
    var sig, freq, pitchEnv, atk, dec, sweep, fb,
        index1, index2, detune, noise, fbIn, car, mod1, mod2,
        indexMod, revSig;

    freq   = \freq.kr(300);
    atk    = \atk.kr(0.01);
    dec    = \dec.kr(1.2);
    fb     = \fb.kr(0.2);
    index1 = \index1.kr(2);
    index2 = \index2.kr(1);
    sweep  = \sweep.kr(3);       // gentle sweep
    detune = 2**(\spread.kr(12) / 1200);
    noise  = \noise.kr(1.5);     // more noise in the mix

    fbIn = LocalIn.ar(2) * \feedback.kr(0.4);
    fbIn = Rotate2.ar(fbIn[0], fbIn[1],
        LFNoise2.ar(0.1) * \fbmod.kr(0.2));

    pitchEnv = (1 + (sweep * Env.perc(0.0, 0.2, curve: -2).ar))
             * XLine.ar(1, 0.6, sweep.reciprocal);

    // index breathes slowly with LFO
    indexMod = SinOsc.kr(LFNoise2.kr(2).exprange(0.2, 3)).range(0.5, 1.5);

    mod1 = SinOsc.ar([freq, freq * detune] * pitchEnv * \ratio1.kr(1))
         * index1 * indexMod;
    mod2 = SinOsc.ar([freq, freq * detune] * pitchEnv * \ratio2.kr(1.5))
         * index2 * (2 - indexMod); // moves opposite to mod1

    car = SinOscFB.ar(
        [freq, freq * detune] * pitchEnv * (1 + mod1 + mod2) + fbIn,
        fb
    ) * EnvGen.kr(Env.perc(atk, dec, 1, -3)); // gentle curve

    sig = car + (WhiteNoise.ar * XLine.ar(1, 0.05, dec * 0.5) * noise
              * EnvGen.kr(Env.perc(atk, dec * 0.6, 1, 0), gate));

    sig = BLowShelf.ar(sig, \lofreq.kr(400), 1, \lodb.kr(-4));
    sig = BHiShelf.ar(sig, \hifreq.kr(6000), 1, \hidb.kr(5));

    // reverb is part of the sound, not just send
    // revSig = GVerb.ar(sig.sum, \roomsize.kr(8), \reverbtime.kr(4),
    //     spread: 20) * -10.dbamp;
    // sig = sig + revSig;

    sig = SelectX.ar(\drivemix.kr(0.1),
        [sig, (sig * \drive.kr(3).dbamp).distort * \drive.kr(3).neg.dbamp * 2]);

    sig = Compander.ar(sig, sig, thresh: 0.6, slopeAbove: 0.6,
        clampTime: 0.01, relaxTime: 2.0);

    sig = HPF.ar(sig, \hpf.kr(80));
    sig = sig * \gain.kr(-20).dbamp;
    sig = Pan2.ar(sig, \pan.kr(0));
    sig = LeakDC.ar(sig, 0.995);
    sig = sig * \amp.kr(1);

    LocalOut.ar(sig + car + mod1 + mod2);
    DetectSilence.ar(sig, doneAction: 2);
    Out.ar(\out.kr(0), sig);
}).add;


// -------------------------------------------------------
// VARIATION 6: fmPercGlitch
// Sample-rate and bit-depth decimated FM. Modulator goes
// through Decimator before feeding carrier. Pitch quantized
// stepwise by a slow SampleAndHold, giving a lo-fi digital
// stepped-pitch feel. Harsh, circuit-bent character.
// -------------------------------------------------------
SynthDef(\fmPercGlitch, {|gate=1|
    var sig, freq, pitchEnv, atk, dec, sweep, fb,
        index1, index2, detune, noise, fbIn, car, mod1, mod2,
        srMod, bitMod;

    freq   = \freq.kr(220);
    atk    = \atk.kr(0.001);
    dec    = \dec.kr(0.25);
    fb     = \fb.kr(1.5);
    index1 = \index1.kr(3);
    index2 = \index2.kr(2);
    sweep  = \sweep.kr(10);
    detune = 2**(\spread.kr(25) / 1200);
    noise  = \noise.kr(0.8);

    fbIn = LocalIn.ar(2) * \feedback.kr(0.8);

    pitchEnv = (1 + (sweep * Env.perc(0.0, 0.1, curve: -5).ar))
             * XLine.ar(1, 0.2, sweep.reciprocal);

    // stepped sample-rate modulation
    srMod  = LFNoise0.kr(\glitchRate.kr(16)).exprange(
        \srMin.kr(4000), \srMax.kr(44100));
    bitMod = LFNoise0.kr(\glitchRate.kr(16) * 0.5).range(
        \bitsMin.kr(4), \bitsMax.kr(12));

    mod1 = PulseDPW.ar([freq, freq * detune] * pitchEnv * \ratio1.kr(1.5),
        \pulseWidth.kr(0.4)) * index1;
    // decimate modulator signal before using it
    // mod1 = Decimator.ar(mod1, srMod, bitMod);

    mod2 = SinOsc.ar([freq, freq * detune] * pitchEnv * \ratio2.kr(3)) * index2;
    mod2 = Decimator.ar(mod2, srMod * 2, bitMod + 2);

    car = SinOscFB.ar(
        [freq, freq * detune] * pitchEnv * (1 + mod1 + mod2) + fbIn,
        fb
    ) * EnvGen.kr(Env.perc(atk, dec, 1, -5));

    sig = car + (WhiteNoise.ar * XLine.ar(1, 0.01, dec * 0.2) * noise
              * EnvGen.kr(Env.perc(atk, dec * 0.15, 1, -6), gate));

    // decimate the output too
    sig = Decimator.ar(sig, srMod * 0.5, bitMod - 2);
    sig = (sig * 2).fold2(1) * 0.7; // fold distortion

    sig = BPeakEQ.ar(sig, \midfreq.kr(2000), 0.8, \middb.kr(6));

    sig = Compander.ar(sig, sig, thresh: 0.6, slopeAbove: 0.5,
        clampTime: 0.001, relaxTime: 0.3);

    sig = HPF.ar(sig, \hpf.kr(150));
    sig = sig * \gain.kr(-18).dbamp;
    sig = Pan2.ar(sig, \pan.kr(0));
    sig = LeakDC.ar(sig, 0.995);
    sig = sig * \amp.kr(1);

    LocalOut.ar(sig + car + mod1 + mod2);
    DetectSilence.ar(sig, doneAction: 2);
    Out.ar(\out.kr(0), sig);
}).add;


"All fmPerc variations loaded!".postln;
)

// ============================================================
// USAGE — single hits
// ============================================================

Synth(\fmPercMetal, [\freq, 300,  \dec, 0.5,  \index1, 5])
Synth(\fmPercWood,  [\freq, 600,  \dec, 0.07, \index1, 2])
Synth(\fmPercChaos, [\freq, 150,  \dec, 0.6,  \feedback, 2, \fbdrive, 4])
Synth(\fmPercThump, [\freq, 55,   \dec, 0.7,  \sweep, 20])
Synth(\fmPercAir,   [\freq, 280,  \dec, 1.5,  \noise, 2])
Synth(\fmPercGlitch,[\freq, 200,  \dec, 0.3,  \glitchRate, 24, \bitsMin, 3])

// ============================================================
// USAGE — sequenced pattern
// ============================================================
(
var defs = [\fmPercMetal, \fmPercWood, \fmPercChaos,
            \fmPercThump, \fmPercAir,  \fmPercGlitch];

Pbind(
    \instrument, Pwrand(defs, [0, 1, 0, 1, 0, 0].normalizeSum, inf),
    \freq,  Pwhite(60, 50).round(Prand([1, 55, 110, 220], inf)),
    \dur,   Pwrand([0.125, 0.25, 0.5, 1.0], [0.5, 0.3, 0.15, 0.05], inf),
    \dur, 0.25,
    \amp,   Pwhite(0.5, 1.0),
    \pan,   Pwhite(-0.7, 0.7),
    // \glitchRate, 4, \bitsMin, 9,
    // \dec,   Pwhite(0.05, 1.0),
    \atk, Pwhite(0, 0.2),
    \spread, 5,
    // \dec, 0.1,
    \dec, Pwhite(0.01, 0.1),
    // \ratio1, 5,
    \index1, 5,
    \index2, Pwhite(0.3, 3),
    // \index2, 1,
    \feedback, Pwhite(0.1, 1.5),
    \fb,  Pwhite(1.2, 0)
).play(t)
)


