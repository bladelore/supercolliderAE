(
SynthDef(\driftingSines_mono, {
	var sig;
	var lfo = LFTri.ar(\lfoFreq.kr(0.01), 1).linlin(-1, 1, 0, 1);
	var freq = \freq.kr(404);
	var freqs = ([freq, freq/2, freq/5, freq/7] + LFNoise2.kr(0.1 ! 4, \pitchDev.kr(15)));
	var amps = ([0.4, 0.3, 0.2, 0.5] + LFNoise2.kr(0.1 ! 4, 0.1));
	var q = 0.8;

	sig = Blip.ar(freqs, \numHarm.kr(0)) * amps;
	sig = BLowPass4.ar(sig, (5000 * lfo), q);
	sig = Splay.ar(sig);
	sig = LeakDC.ar(sig).sanitize;

	sig = sig * 0.25;
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\driftingSines_mono2, {
	var sig;
	var lfo = LFTri.ar(\lfoFreq.kr(0.01), 1).linlin(-1, 1, 0, 1);
	var freq = \freq.kr(404);
	var freqs = (freq + LFNoise2.kr(0.1 ! 4, \pitchDev.kr(15)));
	var amps = ([0.4, 0.3, 0.2, 0.5] + LFNoise2.kr(0.1 ! 4, 0.1));
	var q = 0.8;

	sig = Blip.ar(freqs, \numHarm.kr(0)) * amps;
	// sig = BLowPass4.ar(sig, (5000 * lfo), q);
	sig = Splay.ar(sig);
	sig = LeakDC.ar(sig).sanitize;

	sig = sig * 0.25;
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\driftingSaws_mono, {
	var sig;
	var lfo = LFTri.ar(\lfoFreq.kr(0.01), 1).linlin(-1, 1, 0, 1);
	var freq = \freq.kr(404);
	var freqs = (freq + LFNoise2.kr(0.1 ! 4, \pitchDev.kr(15)));
	var amps = ([0.4, 0.3, 0.2, 0.5] + LFNoise2.kr(0.1 ! 4, 0.1));
	var q = 0.8;

	sig = NeoVarSawOsc.ar(freqs, \pulseWidth.kr(0,5), \waveshape.kr(0.5)) * amps;
	sig = BLowPass4.ar(sig, (5000 * lfo), q);
	sig = Splay.ar(sig);
	sig = LeakDC.ar(sig).sanitize;

	sig = sig * 0.25;
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;

// Synth voice
SynthDef(\west, {
    arg out, t_gate=1, gate=1, killGate=1, fadeIn=0.01,fadeOut=0.01, freq = 220, pitchBendRatio = 1, glide = 0, fm1Ratio = 0.66, fm2Ratio = 3.3, fm1Amount = 0.0, fm2Amount = 0.0,
    vel = 0.7, pressure = 0, timbre = 0, waveShape = 0, waveFolds = 0, envType = 0, attack = 0.04, peak = 10000, decay = 1, amp = 1, lfoShape = 0, lfoFreq = 0.5,
    lfoToFreqAmount = 0, lfoToWaveShapeAmount = 0, lfoToWaveFoldsAmount = 0, lfoToFm1Amount = 0, lfoToFm2Amount = 0,
    lfoToAttackAmount = 0, lfoToPeakAmount = 0, lfoToDecayAmount = 0, lfoToReverbMixAmount = 0, drift = 0, dur=10, pan=0;

    var i_nyquist = SampleRate.ir * 0.5, signal, controlLag = 0.005, i_numHarmonics = 44,
    modFreq, mod1, mod2, mod1Index, mod2Index, mod1Freq, mod2Freq, sinOsc, triOsc, additiveOsc, additivePhase,
    filterEnvVel, filterEnvLow, lpgEnvelope, lpgSignal, asrEnvelope, asrFilterFreq, asrSignal, killEnvelope, i_driftRate = 0.15, maxDecay=8;

    // Make lfos
    var lfo = Select.kr(lfoShape, [
        LFTri.kr(lfoFreq),
        LFSaw.kr(lfoFreq),
        LFPulse.kr(lfoFreq),
        LFDNoise0.kr(lfoFreq * 2)
    ]);

    var lfoArray = Array.fill(9, 0);
    lfoArray[0] = (lfo * lfoToFreqAmount * 18).midiratio; // Freq ratio
    lfoArray[1] = (lfo * lfoToWaveShapeAmount) + LFNoise1.kr(freq: i_driftRate, mul: drift); // Wave Shape
    lfoArray[2] = ((lfo * lfoToWaveFoldsAmount) + LFNoise1.kr(freq: i_driftRate, mul: drift)) * 2; // Wave Folds
    lfoArray[3] = ((lfo * lfoToFm1Amount) + LFNoise1.kr(freq: i_driftRate, mul: drift)) * 0.5; // FM1 Amount
    lfoArray[4] = ((lfo * lfoToFm2Amount) + LFNoise1.kr(freq: i_driftRate, mul: drift)) * 0.5; // FM2 Amount
    lfoArray[5] = ((lfo * lfoToAttackAmount) + LFNoise1.kr(freq: i_driftRate, mul: drift)) * 2.2; // Attack
    lfoArray[6] = (((lfo * lfoToPeakAmount) + LFNoise1.kr(freq: i_driftRate, mul: drift)) * 24).midiratio; // Peak multiplier
    lfoArray[7] = ((lfo * lfoToDecayAmount) + LFNoise1.kr(freq: i_driftRate, mul: drift)) * 2.2; // Decay
    lfoArray[8] = (lfo * lfoToReverbMixAmount) + LFNoise1.kr(freq: i_driftRate, mul: drift); // Reverb Mix


    // LFO ins
    freq = (freq * lfoArray[0]).clip(0, i_nyquist);
    waveShape = (waveShape + lfoArray[1]).clip(0, 1);
    waveFolds = (waveFolds + lfoArray[2]).clip(0, 3);
    fm1Amount = (fm1Amount + lfoArray[3]).clip(0, 1);
    fm2Amount = (fm2Amount + lfoArray[4]).clip(0, 1);
    attack = (attack + lfoArray[5]).clip(0.003, 8);
    peak = (peak * lfoArray[6]).clip(100, 10000);
    decay = (decay + lfoArray[7]).clip(0.01, maxDecay);

    // Lag inputs
    freq = Lag.kr(freq * pitchBendRatio, 0.007 + glide);
    fm1Ratio = Lag.kr(fm1Ratio, controlLag);
    fm2Ratio = Lag.kr(fm2Ratio, controlLag);
    fm1Amount = Lag.kr(fm1Amount.squared, controlLag);
    fm2Amount = Lag.kr(fm2Amount.squared, controlLag);

    vel = Lag.kr(vel, controlLag);
    waveShape = Lag.kr(waveShape, controlLag);
    waveFolds = Lag.kr(waveFolds, controlLag);
    attack = Lag.kr(attack, controlLag);
    peak = Lag.kr(peak, controlLag);
    decay = Lag.kr(decay, controlLag);

    // Modulators
    mod1Index = fm1Amount * 22;
    mod1Freq = freq * fm1Ratio * LFNoise2.kr(freq: 0.1, mul: 0.001, add: 1);
    mod1 = SinOsc.ar(freq: mod1Freq, phase: 0, mul: mod1Index * mod1Freq, add: 0);
    mod2Index = fm2Amount * 12;
    mod2Freq = freq * fm2Ratio * LFNoise2.kr(freq: 0.1, mul: 0.005, add: 1);
    mod2 = SinOsc.ar(freq: mod2Freq, phase: 0, mul: mod2Index * mod2Freq, add: 0);
    modFreq = freq + mod1 + mod2;

    // Sine and triangle
    sinOsc = SinOsc.ar(freq: modFreq, phase: 0, mul: 0.5);
    triOsc = VarSaw.ar(freq: modFreq, iphase: 0, width: 0.5, mul: 0.5);

    // Additive square and saw
    additivePhase = LFSaw.ar(freq: modFreq, iphase: 1, mul: pi, add: pi);
    additiveOsc = Mix.fill(i_numHarmonics, {
        arg index;
        var harmonic, harmonicFreq, harmonicCutoff, attenuation;

        harmonic = index + 1;
        harmonicFreq = freq * harmonic;
        harmonicCutoff = i_nyquist - harmonicFreq;

        // Attenuate harmonics that will go over nyquist once FM is applied
        attenuation = Select.kr(index, [1, // Save the fundamental
            (harmonicCutoff - (harmonicFreq * 0.25) - harmonicFreq).expexp(0.000001, harmonicFreq * 0.5, 0.000001, 1)]);

            (sin(additivePhase * harmonic % 2pi) / harmonic) * attenuation * (harmonic % 2 + waveShape.linlin(0.666666, 1, 0, 1)).min(1);
        }
    );

    // Mix carriers
    signal = LinSelectX.ar(waveShape * 3, [sinOsc, triOsc, additiveOsc]);

    // Fold
    signal = Fold.ar(in: signal * (1 + (timbre * 0.5) + (waveFolds * 2)), lo: -0.5, hi: 0.5);

    // Hack away some aliasing
    signal = LPF.ar(in: signal, freq: 12000);

    // Noise
    signal = signal + PinkNoise.ar(mul: 0.003);

    // LPG
    filterEnvVel = vel.linlin(0, 1, 0.5, 1);
    filterEnvLow = (peak * filterEnvVel).min(300);

    lpgEnvelope = EnvGen.ar(envelope: Env.new(levels: [0, 1, 0], times: [0.003, decay], curve: [4, -20]), gate: t_gate, timeScale: dur);
    lpgSignal = RLPF.ar(in: signal, freq: lpgEnvelope.linlin(0, 1, filterEnvLow, peak * filterEnvVel), rq: 0.9);
    lpgSignal = lpgSignal * EnvGen.ar(envelope: Env.new(levels: [0, 1, 0], times: [0.002, decay], curve: [4, -10]), gate: t_gate, timeScale: dur);

    // ASR with 4-pole filter
    asrEnvelope = EnvGen.ar(envelope: Env.new(levels: [0, 1, 0], times: [attack, decay], curve: -4, releaseNode: 1), gate: gate);
    asrFilterFreq = asrEnvelope.linlin(0, 1, filterEnvLow, peak * filterEnvVel);
    asrSignal = RLPF.ar(in: signal, freq: asrFilterFreq, rq: 0.95);
    asrSignal = RLPF.ar(in: asrSignal, freq: asrFilterFreq, rq: 0.95);
    asrSignal = asrSignal * EnvGen.ar(envelope: Env.asr(attackTime: attack, sustainLevel: 1, releaseTime: decay, curve: -4), gate: gate);

    signal = Select.ar(envType, [lpgSignal, asrSignal]);

    signal = signal * vel.linlin(0, 1, 0.2, 1) ;

    // Saturation amp
    signal = tanh(signal * pressure.linlin(0, 1, 1.5, 3) * amp).softclip;

// main Envelope
        signal = signal * EnvGen.kr(
            Env([0.0,1.0,1.0,0], [fadeIn, fadeOut], releaseNode: 2),  
            gate: killGate,  
            doneAction: 2
        ) ! 2;

    // Pan
    signal = Pan2.ar(signal, pan);
    signal = signal.sanitize;

    Out.ar(out, signal);
}).add;

// Synth voice
SynthDef(\west_mono, {
    arg out, t_gate=1, gate=1, killGate=1, fadeIn=0.01,fadeOut=0.01, freq = 220, pitchBendRatio = 1, glide = 0, fm1Ratio = 0.66, fm2Ratio = 3.3, fm1Amount = 0.0, fm2Amount = 0.0,
    vel = 0.7, pressure = 0, timbre = 0, waveShape = 0, waveFolds = 0, envType = 0, attack = 0.04, peak = 10000, decay = 1, amp = 1, lfoShape = 0, lfoFreq = 0.5,
    lfoToFreqAmount = 0, lfoToWaveShapeAmount = 0, lfoToWaveFoldsAmount = 0, lfoToFm1Amount = 0, lfoToFm2Amount = 0,
    lfoToAttackAmount = 0, lfoToPeakAmount = 0, lfoToDecayAmount = 0, lfoToReverbMixAmount = 0, drift = 0, dur=10, pan=0;

    var i_nyquist = SampleRate.ir * 0.5, signal, controlLag = 0.005, i_numHarmonics = 44,
    modFreq, mod1, mod2, mod1Index, mod2Index, mod1Freq, mod2Freq, sinOsc, triOsc, additiveOsc, additivePhase,
    filterEnvVel, filterEnvLow, lpgEnvelope, lpgSignal, asrEnvelope, asrFilterFreq, asrSignal, killEnvelope, i_driftRate = 0.15, maxDecay=8;

    // Make lfos
    var lfo = Select.kr(lfoShape, [
        LFTri.kr(lfoFreq),
        LFSaw.kr(lfoFreq),
        LFPulse.kr(lfoFreq),
        LFDNoise0.kr(lfoFreq * 2)
    ]);

    var lfoArray = Array.fill(9, 0);
    lfoArray[0] = (lfo * lfoToFreqAmount * 18).midiratio; // Freq ratio
    lfoArray[1] = (lfo * lfoToWaveShapeAmount) + LFNoise1.kr(freq: i_driftRate, mul: drift); // Wave Shape
    lfoArray[2] = ((lfo * lfoToWaveFoldsAmount) + LFNoise1.kr(freq: i_driftRate, mul: drift)) * 2; // Wave Folds
    lfoArray[3] = ((lfo * lfoToFm1Amount) + LFNoise1.kr(freq: i_driftRate, mul: drift)) * 0.5; // FM1 Amount
    lfoArray[4] = ((lfo * lfoToFm2Amount) + LFNoise1.kr(freq: i_driftRate, mul: drift)) * 0.5; // FM2 Amount
    lfoArray[5] = ((lfo * lfoToAttackAmount) + LFNoise1.kr(freq: i_driftRate, mul: drift)) * 2.2; // Attack
    lfoArray[6] = (((lfo * lfoToPeakAmount) + LFNoise1.kr(freq: i_driftRate, mul: drift)) * 24).midiratio; // Peak multiplier
    lfoArray[7] = ((lfo * lfoToDecayAmount) + LFNoise1.kr(freq: i_driftRate, mul: drift)) * 2.2; // Decay
    lfoArray[8] = (lfo * lfoToReverbMixAmount) + LFNoise1.kr(freq: i_driftRate, mul: drift); // Reverb Mix


    // LFO ins
    freq = (freq * lfoArray[0]).clip(0, i_nyquist);
    waveShape = (waveShape + lfoArray[1]).clip(0, 1);
    waveFolds = (waveFolds + lfoArray[2]).clip(0, 3);
    fm1Amount = (fm1Amount + lfoArray[3]).clip(0, 1);
    fm2Amount = (fm2Amount + lfoArray[4]).clip(0, 1);
    attack = (attack + lfoArray[5]).clip(0.003, 8);
    peak = (peak * lfoArray[6]).clip(100, 10000);
    decay = (decay + lfoArray[7]).clip(0.01, maxDecay);

    // Lag inputs
    freq = Lag.kr(freq * pitchBendRatio, 0.007 + glide);
    fm1Ratio = Lag.kr(fm1Ratio, controlLag);
    fm2Ratio = Lag.kr(fm2Ratio, controlLag);
    fm1Amount = Lag.kr(fm1Amount.squared, controlLag);
    fm2Amount = Lag.kr(fm2Amount.squared, controlLag);

    vel = Lag.kr(vel, controlLag);
    waveShape = Lag.kr(waveShape, controlLag);
    waveFolds = Lag.kr(waveFolds, controlLag);
    attack = Lag.kr(attack, controlLag);
    peak = Lag.kr(peak, controlLag);
    decay = Lag.kr(decay, controlLag);

    // Modulators
    mod1Index = fm1Amount * 22;
    mod1Freq = freq * fm1Ratio * LFNoise2.kr(freq: 0.1, mul: 0.001, add: 1);
    mod1 = SinOsc.ar(freq: mod1Freq, phase: 0, mul: mod1Index * mod1Freq, add: 0);
    mod2Index = fm2Amount * 12;
    mod2Freq = freq * fm2Ratio * LFNoise2.kr(freq: 0.1, mul: 0.005, add: 1);
    mod2 = SinOsc.ar(freq: mod2Freq, phase: 0, mul: mod2Index * mod2Freq, add: 0);
    modFreq = freq + mod1 + mod2;

    // Sine and triangle
    sinOsc = SinOsc.ar(freq: modFreq, phase: 0, mul: 0.5);
    triOsc = VarSaw.ar(freq: modFreq, iphase: 0, width: 0.5, mul: 0.5);

    // Additive square and saw
    additivePhase = LFSaw.ar(freq: modFreq, iphase: 1, mul: pi, add: pi);
    additiveOsc = Mix.fill(i_numHarmonics, {
        arg index;
        var harmonic, harmonicFreq, harmonicCutoff, attenuation;

        harmonic = index + 1;
        harmonicFreq = freq * harmonic;
        harmonicCutoff = i_nyquist - harmonicFreq;

        // Attenuate harmonics that will go over nyquist once FM is applied
        attenuation = Select.kr(index, [1, // Save the fundamental
            (harmonicCutoff - (harmonicFreq * 0.25) - harmonicFreq).expexp(0.000001, harmonicFreq * 0.5, 0.000001, 1)]);

            (sin(additivePhase * harmonic % 2pi) / harmonic) * attenuation * (harmonic % 2 + waveShape.linlin(0.666666, 1, 0, 1)).min(1);
        }
    );

    // Mix carriers
    signal = LinSelectX.ar(waveShape * 3, [sinOsc, triOsc, additiveOsc]);

    // Fold
    signal = Fold.ar(in: signal * (1 + (timbre * 0.5) + (waveFolds * 2)), lo: -0.5, hi: 0.5);

    // Hack away some aliasing
    signal = LPF.ar(in: signal, freq: 12000);

    // Noise
    signal = signal + PinkNoise.ar(mul: 0.003);

    // LPG
    filterEnvVel = vel.linlin(0, 1, 0.5, 1);
    filterEnvLow = (peak * filterEnvVel).min(300);

    lpgEnvelope = EnvGen.ar(envelope: Env.new(levels: [0, 1, 0], times: [0.003, decay], curve: [4, -20]), gate: t_gate, timeScale: dur);
    lpgSignal = RLPF.ar(in: signal, freq: lpgEnvelope.linlin(0, 1, filterEnvLow, peak * filterEnvVel), rq: 0.9);
    lpgSignal = lpgSignal * EnvGen.ar(envelope: Env.new(levels: [0, 1, 0], times: [0.002, decay], curve: [4, -10]), gate: t_gate, timeScale: dur);

    // ASR with 4-pole filter
    asrEnvelope = EnvGen.ar(envelope: Env.new(levels: [0, 1, 0], times: [attack, decay], curve: -4, releaseNode: 1), gate: gate);
    asrFilterFreq = asrEnvelope.linlin(0, 1, filterEnvLow, peak * filterEnvVel);
    asrSignal = RLPF.ar(in: signal, freq: asrFilterFreq, rq: 0.95);
    asrSignal = RLPF.ar(in: asrSignal, freq: asrFilterFreq, rq: 0.95);
    asrSignal = asrSignal * EnvGen.ar(envelope: Env.asr(attackTime: attack, sustainLevel: 1, releaseTime: decay, curve: -4), gate: gate);

    signal = Select.ar(envType, [lpgSignal, asrSignal]);

    signal = signal * vel.linlin(0, 1, 0.2, 1) ;

    // Saturation amp
    signal = tanh(signal * pressure.linlin(0, 1, 1.5, 3) * amp).softclip;

// // main Envelope
//         signal = signal * EnvGen.kr(
//             Env([0.0,1.0,1.0,0], [fadeIn, fadeOut], releaseNode: 2),  
//             gate: killGate,  
//             doneAction: 2
//         ) ! 2;

    // Pan
    signal = Pan2.ar(signal, pan);
    signal = signal.sanitize;

    Out.ar(out, signal);
}).add;

SynthDef(\pulsePluck, {
    var sig;
    var gate = \gate.kr(1);
    // var atk = \atk.kr(0.01);
    // var rel = \rel.kr(8);
    var detune = 2**(\detune.kr(10) / 1200);
    var freq = \freq.kr(60);
    var envDepth = \envDepth.kr(1);
    var env = Env.adsr(attackTime: \atk.kr(0.01), decayTime: \dec.kr(0.5), sustainLevel: \sus.kr(0), releaseTime: \rel.kr(0.2), curve: -4.0);
    var modEnv = EnvGen.kr(Env.perc(\filterAtk.kr(0.01), \filterDec.kr(4), curve: -8), gate) * envDepth;
    var lfoArray = Array.fill(9, 0);
    var i_driftRate = \driftRate.kr(2);
    var drift = \drift.kr(1);
    var lfoShape = \lfoShape.kr(0);
    var lfoFreq = \lfoFreq.kr(0.5);
    // var lfoMod = SinOsc.ar(0.1);
    var lfo = Select.kr(lfoShape, [
        LFTri.kr(lfoFreq),
        LFSaw.kr(lfoFreq),
        LFPulse.kr(lfoFreq),
        LFDNoise0.kr(lfoFreq * 2)
    ]);
    var width, shape, filterFreq, morph;
    var filter = \filter.kr(50);
    var filter2 = \filter2.kr(5000);
    var pitchEnv = \pitchEnv.kr(0).lincurve(0,1,0,1,-4) * freq * modEnv.lincurve(0,1,0,1,-4);
    freq = freq + pitchEnv;
    lfoArray[0] = (lfo * \lfoToWidth.kr(0));
    lfoArray[1] = (lfo * \lfoToShapeAmount.kr(0)) + LFNoise1.kr(freq: i_driftRate, mul: drift); // Wave Shape
    lfoArray[2] = (lfo * \lfoToFilter.kr(0) * 18).midiratio; // Freq ratio
    lfoArray[3] = (lfo * \lfoToMorph.kr(0)); // Freq ratio
    width = (\width.kr(0.5) + modEnv + lfoArray[0]).clip(0,1);
    shape = (\shape.kr(0.5) + modEnv + lfoArray[1]).clip(0,1);
    filterFreq = (filter + (modEnv * filter) * lfoArray[2]).clip(0, 20000);
    // morph = (\morph.kr(0.5) + modEnv + lfoArray[3]).clip(0,1);
    morph = (\morph.kr(0.5) + modEnv + lfoArray[3]).clip(-1,1);
    // sig = NeoVarSawOsc.ar([freq, freq * detune], width, shape);
    // sig = PulseDPW.ar([freq, freq * detune], width);
    sig = VosimOsc.ar([freq, freq * detune], freq+(freq * width), freq, shape*10);
    sig = VASEM12.ar(sig, filterFreq, res: 0.5, transition: morph);
    sig = RLPF.ar(sig, filter2);
    sig = sig * EnvGen.ar(env, gate, doneAction: Done.freeSelf);
    // sig = Splay.ar(sig);
    sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
    sig = sig * \gain.kr(0).dbamp;
    sig = sig * \amp.kr(1);
    Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\pulsePluck_mono, {
    var sig;
    // var gate = \gate.kr(1);
    // var atk = \atk.kr(0.01);
    // var rel = \rel.kr(8);
    var detune = 2**(\detune.kr(10) / 1200);
    var freq = \freq.kr(60);
    var envDepth = \envDepth.kr(1);
    var env = Env.adsr(attackTime: \atk.kr(0.01), decayTime: \dec.kr(0.5), sustainLevel: \sus.kr(0), releaseTime: \rel.kr(0.2), curve: -4.0);
    var modEnv = EnvGen.kr(Env.perc(\filterAtk.kr(0.01), \filterDec.kr(4), curve: -8)) * envDepth;
    var lfoArray = Array.fill(9, 0);
    var i_driftRate = \driftRate.kr(2);
    var drift = \drift.kr(1);
    var lfoShape = \lfoShape.kr(0);
    var lfoFreq = \lfoFreq.kr(0.5);
    // var lfoMod = SinOsc.ar(0.1);
    var lfo = Select.kr(lfoShape, [
        LFTri.kr(lfoFreq),
        LFSaw.kr(lfoFreq),
        LFPulse.kr(lfoFreq),
        LFDNoise0.kr(lfoFreq * 2)
    ]);
    var width, shape, filterFreq, morph;
    var filter = \filter.kr(50);
    var filter2 = \filter2.kr(5000);
    var pitchEnv = \pitchEnv.kr(0).lincurve(0,1,0,1,-4) * freq * modEnv.lincurve(0,1,0,1,-4);
    freq = freq + pitchEnv;
    lfoArray[0] = (lfo * \lfoToWidth.kr(0));
    lfoArray[1] = (lfo * \lfoToShapeAmount.kr(0)) + LFNoise1.kr(freq: i_driftRate, mul: drift); // Wave Shape
    lfoArray[2] = (lfo * \lfoToFilter.kr(0) * 18).midiratio; // Freq ratio
    lfoArray[3] = (lfo * \lfoToMorph.kr(0)); // Freq ratio
    width = (\width.kr(0.5) + modEnv + lfoArray[0]).clip(0,1);
    shape = (\shape.kr(0.5) + modEnv + lfoArray[1]).clip(0,1);
    filterFreq = (filter + (modEnv * filter) * lfoArray[2]).clip(0, 20000);
    // morph = (\morph.kr(0.5) + modEnv + lfoArray[3]).clip(0,1);
    morph = (\morph.kr(0.5) + modEnv + lfoArray[3]).clip(-1,1);
    // sig = NeoVarSawOsc.ar([freq, freq * detune], width, shape);
    // sig = PulseDPW.ar([freq, freq * detune], width);
    sig = VosimOsc.ar([freq, freq * detune], freq+(freq * width), freq, shape*10);
    sig = VASEM12.ar(sig, filterFreq, res: 0.5, transition: morph);
    sig = RLPF.ar(sig, filter2);
    // sig = sig * EnvGen.ar(env, gate, doneAction: Done.freeSelf);
    // sig = Splay.ar(sig);
    sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
    sig = sig * \gain.kr(0).dbamp;
    sig = sig * \amp.kr(1);
    Out.ar(\out.kr(0), sig);
}).add;
)