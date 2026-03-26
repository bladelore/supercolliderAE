// =============================================================================
// AdditiveChain
// Spectral chain processor for additive synthesis.
//
// Usage (UGen context):
//   AdditiveChain(32, 440)
//       .makeStretchedHarmonicSeries(0.01)  // UGen-time
//       .addSpectralTilt(-3)                // UGen-time
//       .lpFilter(2000)                     // UGen-time
//       .oscBank                             // -> AdditiveStereo
//       .simplePan(0.5, 1.0)               // on AdditiveStereo
//       .midSideSpread(0.8)                 // on AdditiveStereo
//       .air(0.5, 0.3, 0.2, 0.8)           // on AdditiveStereo
//       .render                             // unwrap [left, right]
//
// Usage (language context):
//   AdditiveChain(32, 440)
//       .makeStretchedHarmonicSeries(0.01)
//       .evenOddMask(1, 0.5)
//       .freqs   // access array directly
// =============================================================================

AdditiveChain {

    var <>freq, <>numPartials, <>ratios, <>amps, <>phases, <>freqs;

    *new { |numPartials, freq|
        ^super.new.init(numPartials, freq)
    }

    init { |argNumPartials, argFreq|
        freq        = argFreq;
        numPartials = argNumPartials;
        ratios      = (1..argNumPartials);
        amps        = 1 ! argNumPartials;
        phases      = 0 ! argNumPartials;
        ^this
    }

    // piecewise linear transfer function
    prTransferFunc { |phase, skew|
        ^Select.kr(phase > skew, [
            0.5 * phase / skew,
            0.5 * (1 + ((phase - skew) / (1 - skew)))
        ])
    }

    // raised cosine window
    prRaisedCos { |phase, index|
        var cosine  = cos(phase * 2pi);
        var raised  = exp(index.abs * (cosine - 1));
        var hanning = 0.5 * (1 + cos(phase * 2pi));
        ^raised * hanning
    }

    // Sets even/odd partial amplitudes independently
    evenOddMask { |oddLevel, evenLevel|
        amps = amps.collect { |item, i|
            if(i.odd) { item * oddLevel } { item * evenLevel }
        };
        ^this
    }

    // Gates partials below threshold
    ampAbove { |threshold|
        var logAmps   = amps.ampdb;
        var logThresh = threshold.lincurve(0, 1, -120, 0, -4);
        var gate      = logAmps > logThresh;
        amps = amps * gate;
        ^this
    }

    ampBelow { |threshold|
        var logAmps   = amps.ampdb;
        var logThresh = threshold.lincurve(0, 1, -120, 0, -4);
        var gate      = logAmps < logThresh;
        amps = amps * gate;
        ^this
    }

    ampSlew { |up=0.1, down=0.1|
        amps = Lag3UD.ar(amps, up, down);
        ^this
    }

    // Normalises amps to a target dB level
    ampNormalise { |ampDb=0, amount=1|
        var normalised = amps / amps.max(0.0001) * ampDb.dbamp;
        amps = amps.blend(normalised, amount);
        ^this
    }

    // Fades out partials approaching the Nyquist frequency
    limiter {
        var nyquist   = SampleRate.ir * 0.5;
        var fadeStart = nyquist - 2000;
        var limiter   = (1 - (freqs - fadeStart) / 1000).clip(0, 1);
        amps          = amps * limiter;
        ^this
    }

    // Generates a stretched harmonic series (inharmonicity > 0 = stretched)
    makeStretchedHarmonicSeries { |inharmonicity|
        freqs = freq * ratios
            * (1 + (inharmonicity * ratios * ratios)).sqrt;
        ^this
    }

    // Applies a dB-per-octave amplitude slope across partials
    spectralTilt { |tiltPerOctave|
        amps = amps * (ratios.log2 * tiltPerOctave).dbamp;
        ^this
    }

    // Warps the frequency spacing of partials using a transfer function
    warpFrequencies { |warpPoint|
        var normalizedFreqs = (freqs / freqs[numPartials - 1]).clip(0, 1);
        var warpedFreqs     = this.prTransferFunc(normalizedFreqs, 1 - warpPoint);
        freqs = warpedFreqs * freqs[numPartials - 1];
        ^this
    }

    // -------------------------------------------------------------------------
    // waveforms
    // -------------------------------------------------------------------------

    // Pulse-width style spectral shaping (from RAZOR)
    pulseWidth { |width, tilt, lvl, shift|
        var deltaPitch   = shift.ratiomidi;
        var idx          = ratios;
        var pitch        = freq.cpsmidi;
        var partialPitch = (ratios * freq).cpsmidi;
        var overallTilt;

        lvl   = lvl.dbamp;
        width = (width - 1) * -0.25 * shift * idx;
        width = width.wrap(0, 2pi);
        width = sin(width).abs;
        width = lvl * 1.333 * width;

        overallTilt = (partialPitch - (pitch + deltaPitch)) * (tilt - 6) * (1/12);
        overallTilt = overallTilt.dbamp;

        amps  = overallTilt * width;
        freqs = freq ! numPartials;
        ^this
    }

    // -------------------------------------------------------------------------
    // Dissonance
    // -------------------------------------------------------------------------

    // Detunes every nth partial by a scaled amount
    partialDetune { |amount=0.1, partialSelect=2, mode=0|
        var idx       = ratios - 1;
        var detune    = ((50 / freq) * amount) - mode;
        var whichIdx  = ((idx / partialSelect).floor * partialSelect);
        var isDetuned = (idx - whichIdx).clip(0, 1);
        detune = detune * isDetuned;
        detune = (freqs ? 0) + (freq * detune);
        freqs  = detune;
        ^this
    }

    // Stretches partials to simulate stiff string inharmonicity
    stiffString { |amount=0.5|
        var scaledAmt = amount.linlin(0, 1, -140, 60);
        var db2AF     = scaledAmt.dbamp;
        var idx       = ratios;
        var newRatio  = (idx.squared * db2AF) + 1;
        newRatio = ((log2(newRatio) * 0.5) ** 2) * idx + 1;
        freqs = (freqs ? 0) + (freq * newRatio);
        ^this
    }

    // Transposes all partials by semitones
    transpose { |semitones|
        freqs = freqs * semitones.midiratio;
        ^this
    }

    // Morphs partial frequencies toward a target frequency
    centroid { |amount, targetFreq|
        var partialPitch = freqs;
        var pitchArr     = targetFreq ! numPartials;
        freqs = amount.linlin(0, 1, partialPitch, pitchArr);
        ^this
    }

    // Reverses the spectral content
    reverse { |amount=1|
        var amountRev    = 1 - amount;
        var reversePitch = amountRev.linlin(
            0, 1,
            ratios.ratiomidi,
            (321.ratiomidi - 320.ratiomidi) ! numPartials
        );
        var newRatio = reversePitch.midiratio;
        freqs = newRatio * freqs;
        // TODO: amps
        ^this
    }

    // -------------------------------------------------------------------------
    // Filters
    // -------------------------------------------------------------------------

    // Butterworth Low Pass Filter
    lpFilter { |argFreq, qFactor=2|
        var response = 1.0 / sqrt(1.0 + pow((freqs / argFreq), (2 * qFactor)));
        amps = amps * response;
        ^this
    }

    // Butterworth High Pass Filter
    hpFilter { |argFreq, qFactor=2|
        var response = 1.0 / sqrt(1.0 + pow((argFreq / freqs), (2 * qFactor)));
        amps = amps * response;
        ^this
    }

    // Notch filter with exponential attenuation around notchFreq
    notchFilter { |notchFreq, notchWidth|
        var dist        = (freqs - notchFreq).abs;
        var attenuation = 1.0 - exp(dist.neg / notchWidth);
        amps = amps * attenuation;
        ^this
    }

    // Morphable filter: morph 0=LPF, 0.5=BPF, 1=HPF
    morphFilter { |argFreq, order=2, morph=0|
        var lpfResponse, bpfResponse, hpfResponse, finalResponse;
        var lpfWeight, bpfWeight, hpfWeight;

        morph = morph.wrap(0, 1) * 2;

        lpfResponse = 1.0 / sqrt(1.0 + pow((freqs / argFreq), (2 * order)));
        hpfResponse = 1.0 / sqrt(1.0 + pow((argFreq / freqs), (2 * order)));
        bpfResponse = lpfResponse * hpfResponse;

        lpfWeight = (1 - morph).clip(0, 1);
        bpfWeight = (1 - (morph - 1).abs).clip(0, 1);
        hpfWeight = (morph - 1).clip(0, 1);

        finalResponse = (lpfResponse * lpfWeight)
            + (bpfResponse * bpfWeight)
            + (hpfResponse * hpfWeight);

        amps = amps * finalResponse;
        ^this
    }

    // All-pass filter — shifts phase around freq
    allPassFilter { |argFreq, q|
        var bandwidth  = argFreq / q;
        var phaseShift = ((freqs - argFreq) / bandwidth).atan * 2;
        phases         = phases + phaseShift;
        ^this
    }

    // Applies a set of formant peaks to the amplitude spectrum
    // formants: array of Events with keys \freq, \bandwidth, \strength
    generateFormants { |formants|
        amps = amps.collect { |amp, i|
            var f = freqs[i];
            var contribution = formants.collect { |formant|
                var centerFreq = formant[\freq];
                var bandwidth  = formant[\bandwidth];
                var strength   = formant[\strength];
                strength * exp(((f - centerFreq).squared).neg / (2 * bandwidth.squared))
            }.sum;
            amp * contribution
        };
        ^this
    }

    // Comb filter with raised cosine profile and warpable phase
    combFilter { |combOffset, combDensity, combSkew, combPeak|
        var phase, warpedPhase;
        phase       = freqs.max(0.001).log2 - freq.max(0.001).log2;
        phase       = (phase * combDensity - combOffset).wrap(0, 1);
        warpedPhase = this.prTransferFunc(phase, combSkew);
        amps        = amps * this.prRaisedCos(warpedPhase, combPeak);
        ^this
    }

    // -------------------------------------------------------------------------
    // Analysis
    // -------------------------------------------------------------------------

    // Tracks sinusoidal partials from a live signal
    extractSines { |sig, freqLag, ampLag, order, transpose,
                    winSize=512, fftSize=4096, hopSize=4, numSines=50|
        var analysis, f, a;

        analysis = FluidSineFeature.kr(
            sig,
            order:       order,
            numPeaks:    numPartials,
            maxNumPeaks: numSines,
            windowSize:  winSize,
            fftSize:     fftSize,
            hopSize:     hopSize,
        );

        transpose = transpose.midiratio;
        freqs     = analysis[0].lag(freqLag, freqLag) + 0.00001 * transpose;
        amps      = analysis[1].lag(ampLag, ampLag);
        ^this
    }

    // Tracks sinusoidal partials, latching frequency on low amplitude
    extractSinesSmooth { |sig, freqLag, ampLag, order, transpose,
                          winSize=512, fftSize=4096, hopSize=4, thresh=0, numSines=50|
        var analysis, f, a;

        analysis = FluidSineFeature.kr(
            sig,
            order:       order,
            numPeaks:    numPartials,
            maxNumPeaks: numSines,
            windowSize:  winSize,
            fftSize:     fftSize,
            hopSize:     hopSize,
        );

        transpose = transpose.midiratio;
        amps      = analysis[1].lag(ampLag, ampLag);
        freqs     = Latch.kr(analysis[0], amps > thresh).lag(freqLag, freqLag) + 0.00001 * transpose;
        ^this
    }

    // -------------------------------------------------------------------------
    // Scale quantisation
    // -------------------------------------------------------------------------

    // Quantizes partial frequencies toward the nearest scale degree
    quantizePartials { |scale, strength=1, baseFreq=(60.midicps)|
        var f              = freqs;
        var scaleRatios    = scale.ratios;
        var buf            = scaleRatios.as(LocalBuf);
        var octave         = (f / baseFreq).log2.floor;
        var position       = IndexInBetween.kr(buf, (f / baseFreq * (2 ** octave.neg)));
        var scaleDegree    = position.round;
        var quantizedFreq  = baseFreq * Index.kr(buf, scaleDegree) * (2 ** octave);
        var distance       = (position - scaleDegree).abs;
        var scaledStrength = strength * (1 - distance);
        freqs = (quantizedFreq * scaledStrength) + (f * (1 - scaledStrength));
        ^this
    }

    // -------------------------------------------------------------------------
    // Oscillator bank
    // -------------------------------------------------------------------------

    prResolve { |val, default|
        ^if(val.isKindOf(Function), { val.value(this) }, { val ?? { default } })
    }

    function { |freq, phase, amp|
        freqs  = this.prResolve(freq,  freqs);
        phases = this.prResolve(phase, phases);
        amps   = this.prResolve(amp,   amps);
        ^this
    }

    oscBank { |randomPhase=0|
        var phase = phases + ({ Rand(0, 2pi) } ! numPartials * randomPhase);
        ^AdditiveStereo(this, SinOsc.ar(freq: freqs, phase: phase, mul: amps))
    }
}


// =============================================================================
// PadSynthChain : AdditiveChain
// Extends AdditiveChain with PADsynth-style harmonic distribution.
//
// Usage:
//   PadSynthChain(10, 5, 80)
//       .padSynthDistribution(harmonicRatio: 1, bw: 10)
//       .spectralTilt(-3)
//       .oscBank
//       .simplePan(sig, 0.5, 1.0)  // implicit transition to AdditiveStereo
//       .render
// =============================================================================

PadSynthChain : AdditiveChain {

    var <>sidebands, <>harmonicIdx, <>sidebandIdx;

    *new { |harmonics=10, sidebands=5, freq=80|
        ^super.new.initPad(harmonics, sidebands, freq)
    }

    initPad { |harmonics, argSidebands, argFreq|
        var argNumPartials = harmonics * argSidebands;
        freq        = argFreq;
        numPartials = argNumPartials;
        ratios      = (1..argNumPartials);
        amps        = 1 ! argNumPartials;
        phases      = 0 ! argNumPartials;
        sidebands   = argSidebands;
        harmonicIdx = (1..harmonics).dupEach(argSidebands);
        sidebandIdx = (0..argSidebands - 1).wrapExtend(argNumPartials);
        ^this
    }

    // Gaussian-ish profile for sideband amplitude distribution
    prHprofile { |fi, bwi, windowSkew=0.5|
        var x = abs(fi - windowSkew) * 2;
        x = x / bwi;
        x = x * x;
        ^exp(x.neg)
    }

    // Distributes harmonic energy into sidebands (PADsynth algorithm)
    padSynthDistribution { |harmonicRatio=1, bw=1, bwScale=1, bwSkew=1, stretch=1, windowSkew=0.5|
        var powN      = pow(harmonicIdx, harmonicRatio / 2);
        var relF      = powN * ((1.0 + (powN - 1)) * stretch);
        var idxOffset = (sidebands / 2).floor;
        var bw_Hz, bwi, partialIdx, freqOffset;

        freqs  = relF * freq;
        bw_Hz  = pow(2, (bw / 1200) - bwSkew) * freq * pow(relF, bwScale);
        bwi    = (1 / harmonicIdx) * bwScale;

        freqOffset = bw_Hz / sidebands;
        partialIdx = sidebandIdx - idxOffset;
        freqs      = freqs + (partialIdx * freqOffset);
        freqs      = freqs.abs;

        amps = this.prHprofile(
            sidebandIdx.linlin(0, sidebands - 1, 0, 1),
            bwi,
            windowSkew
        );
        ^this
    }

}


// =============================================================================
// AdditiveStereo
// Stereo signal renderer for AdditiveChain. Created by calling .oscBank on an
// AdditiveChain, which renders the oscillator bank and transitions to this class.
// Holds both the rendered signal and a reference to the chain.
// Call .render to unwrap the final [left, right] array.
//
// Usage:
//   AdditiveChain(32, 440)
//       .makeStretchedHarmonicSeries(0.01)
//       .oscBank                            // -> AdditiveStereo
//       .simplePan(0.5, 1.0)
//       .midSideSpread(0.8)
//       .air(0.5, 0.3, 0.2, 0.8)
//       .render                             // -> [left, right]
// =============================================================================

AdditiveStereo {

    var <>sig, <>chain;

    *new { |chain, sig|
        ^super.new.init(chain, sig)
    }

    init { |aChain, aSig|
        chain = aChain;
        sig   = aSig;
        ^this
    }

    // Unwrap the stereo signal
    render { ^sig }

    freq        { ^chain.freq }
    numPartials { ^chain.numPartials }
    freqs       { ^chain.freqs }
    amps        { ^chain.amps }
    ratios      { ^chain.ratios }
    phases      { ^chain.phases }

    // Forwards unknown messages to sig, allowing AdditiveStereo to behave
    // transparently as an array in any context that expects a signal.
    doesNotUnderstand { |selector ...args|
        ^sig.perform(selector, *args)
    }

    // pan law
    prPanLaw { |in, pos|
        var left  = in * (4 - (1 - pos)) * (1 - pos) * 0.333333;
        var right = in * (4 - (1 + pos)) * (1 + pos) * 0.333333;
        ^[left.sum, right.sum]
    }

    simplePan { |amount, ramp|
        var partialPitch, pitch, length, end;
        var rampIndices, centerIndices;
        var x0, y0, m0, x, line;

        partialPitch  = chain.freqs.cpsmidi;
        pitch         = chain.freq.cpsmidi;
        length        = ramp.linlin(0, 1, chain.freqs[0], chain.freqs[chain.numPartials - 1]).cpsmidi;
        end           = length;

        rampIndices   = 1 - (partialPitch.floor / end.floor).floor.clip(0, 1);
        centerIndices = (0.5 - rampIndices).clip(0, 1);

        x0 = end; y0 = 1; m0 = 1 / length.max(12); x = partialPitch;
        line = ((x - x0 * m0 + y0) * rampIndices * amount) + centerIndices;
        line = line.linlin(0, 1, -1, 1).lag(0.1, 0.1);
        sig = this.prPanLaw(sig, line);
        ^this
    }

    autoPan { |amount, ramp, saw, cycles|
        var partialPitch, partialPitchScaled, pitch, length, end;
        var rampIndices, centerIndices;
        var x0, y0, m0, x, line;

        partialPitch       = chain.freqs.cpsmidi;
        partialPitchScaled = partialPitch - 80;
        pitch              = chain.freq.cpsmidi;

        cycles             = ((1 - cycles) * 30.neg).dbamp * cycles;
        partialPitchScaled = partialPitchScaled * cycles;
        saw                = saw * 0.5.neg;
        partialPitchScaled = partialPitchScaled + saw;
        partialPitchScaled = partialPitchScaled - partialPitchScaled.collect(_.round);
        partialPitchScaled = partialPitchScaled * (8 - (partialPitchScaled.abs * 16));

        length        = ramp.linlin(0, 1, chain.freqs[0], chain.freqs[chain.numPartials - 1]).cpsmidi;
        end           = length;
        rampIndices   = 1 - (partialPitch.floor / end).floor.clip(0, 1);
        centerIndices = 1 - rampIndices;

        x0 = end; y0 = amount; m0 = 1 / length.max(12); x = partialPitch;
        line = ((x - x0 * m0 + y0) * rampIndices) + centerIndices;
        line = (line * partialPitchScaled).lag(0.1, 0.1);
        sig = this.prPanLaw(sig, line);
        ^this
    }

    stereoSpread { |amount, ramp, saw, cycles|
        var idx, partialPitch, partialPitchScaled, pitch, length, end;
        var rampIndices, centerIndices, spreadIndices;
        var x0, y0, m0, x, line;

        idx                = chain.ratios;
        partialPitch       = chain.freqs.cpsmidi;
        partialPitchScaled = partialPitch - 80;
        pitch              = chain.freq.cpsmidi;

        cycles             = ((1 - cycles) * 30.neg).dbamp * cycles;
        partialPitchScaled = partialPitchScaled * cycles;
        saw                = saw * 0.5.neg;
        partialPitchScaled = partialPitchScaled + saw;
        partialPitchScaled = partialPitchScaled - partialPitchScaled.collect(_.round);
        partialPitchScaled = partialPitchScaled * (8 - (partialPitchScaled.abs * 16));

        length        = ramp.linlin(0, 1, chain.freqs[0], chain.freqs[chain.numPartials - 1]).cpsmidi;
        end           = length;
        rampIndices   = 1 - (partialPitch.floor / end).floor.clip(0, 1);
        centerIndices = 1 - rampIndices;

        // flip polarity of every second partial
        spreadIndices      = idx.wrap(0, 1).linlin(0, 1, -1, 1);
        partialPitchScaled = partialPitchScaled * spreadIndices;

        x0 = end; y0 = amount; m0 = 1 / length.max(12); x = partialPitch;
        line = ((x - x0 * m0 + y0) * rampIndices) + centerIndices;
        line = line * partialPitchScaled;
        sig = this.prPanLaw(sig, line);
        ^this
    }

    // -------------------------------------------------------------------------
    // Per-partial perc envelope with skewed attack and release times
    // atkSkew/relSkew: -1 = low partials slow, high fast; 1 = low fast, high slow
    ampEnv { |gate, atk=0.01, rel=0.5, atkSkew=0, relSkew=0|
        var n       = chain.numPartials;
        var ramp    = (0, 1/(n-1) .. 1);
        var atks    = atk * ramp.linlin(0, 1, 1 - atkSkew, 1 + atkSkew).abs;
        var rels    = rel * ramp.linlin(0, 1, 1 - relSkew, 1 + relSkew).abs;
        var envs    = n.collect { |i|
            EnvGen.ar(Env.perc(atks[i], rels[i]), gate, doneAction: 0)
        };
        sig = sig * envs;
        ^this
    }

    // Stereo processing
    // -------------------------------------------------------------------------

    // Per-partial amplitude envelope with skewed attack and release times
    // atkSkew/relSkew: -1 = low partials slow, high fast; 1 = low fast, high slow
    // Spreads even partials to mid and odd partials to sides
    // amount: 0 = mono, 1 = full spread
    // width:  scales mid level independently
    midSideSpread { |amount=1, midAmp=1, sideAmp=1|
        var mid  = sig[0,2..].sum * midAmp;
        var side = sig[1,3..].sum * sideAmp;
        sig = mid + ([-1,1] * side * amount);
        ^this
    }

    air { |amount, speed, min, max|
        var partialFreqs = chain.freqs;
        var noise_L, noise_R;
        var idxAmp, rangeAmp, ampScale;
        var amtFade, amtMult;

        speed  = speed.linlin(0, 1, -200, 0).midiratio;
        min    = min.linlin(0, 1, -70, 40);
        max    = max.linlin(0, 1, -70, 40);

        noise_L = partialFreqs * speed + 0.333;
        noise_R = partialFreqs * speed + 0.456;

        noise_L = LFBrownNoise2.ar(noise_L, 1, 1, 5);
        noise_R = LFBrownNoise2.ar(noise_R, 1, 1, 5);

        idxAmp   = (chain.amps * (chain.ratios - 1)).max(0.0001).dbamp - max;
        rangeAmp = 1 / (max - min).max(0.1).neg;
        ampScale = (idxAmp * rangeAmp).clip(0, 1) * amount;

        amtFade = (1 - ampScale);
        amtFade = (2 - amtFade) * amtFade;
        amtMult = (2 - ampScale) * ampScale * 2;

        noise_L = (noise_L * amtMult) + amtFade;
        noise_R = (noise_R * amtMult) + amtFade;
        sig = [(noise_L * sig).sum, (noise_R * sig).sum];
        ^this
    }

}


// =============================================================================
// AnalysisBase : AdditiveChain
// Shared base class for offline analysis subclasses.
// Holds storage, playback and phase accumulation logic.
// Subclasses override *analyse and analyse to implement specific analysis.
// =============================================================================

AnalysisBase : AdditiveChain {

    var <>freqFrames, <>ampFrames, <>phaseFrames, <>numFrames, <>sampleRate, <>windowSize,
        <>hopSize, <>freqBuf, <>ampBuf, <>phaseBuf;

    initAnalysis { |argNumPartials|
        numPartials = argNumPartials;
        ^this
    }

    asControlInput {
        ^[freqBuf.bufnum, ampBuf.bufnum, phaseBuf.bufnum, hopSize, numPartials]
    }

    // Accumulates phase across frames given freqFrames and initial phase array
    prAccumPhases { |argFreqFrames, initPhases, argHopSize|
        var accumulated = Array.newClear(argFreqFrames.size);
        argFreqFrames.do { |frame, frameIdx|
            accumulated[frameIdx] = frame.collect { |f, partialIdx|
                var phaseIncr = f * argHopSize / sampleRate * 2pi;
                var prevPhase = if(frameIdx == 0) {
                    initPhases[partialIdx]
                } {
                    accumulated[frameIdx - 1][partialIdx]
                };
                prevPhase + phaseIncr
            };
        };
        ^accumulated
    }

    loadBuffers { |server, action|
        var freqData  = freqFrames.flatten;
        var ampData   = ampFrames.flatten;
        var phaseData = phaseFrames.flatten;

        freqBuf = Buffer.loadCollection(server, freqData,  numPartials, action: {
            ampBuf = Buffer.loadCollection(server, ampData,  numPartials, action: {
                phaseBuf = Buffer.loadCollection(server, phaseData, numPartials, action: {
                    action.value(this);
                });
            });
        });
        ^this
    }

}


// =============================================================================
// AdditiveWavetable : AnalysisBase
// Analyses a source buffer with FluidBufSTFT. Picks bins with highest variance
// across frames for maximum timbral movement.
//
// Usage:
//   AdditiveWavetable.analyse(s, srcBuf, numPartials: 32, action: { |wt|
//       ~wt = wt;
//       ~wt.loadBuffers(s);
//   });
//   ~wt.readPhase(Phasor.ar(0, 1/s.sampleRate, 0, 1)).spectralTilt(-3).oscBank
// =============================================================================

AdditiveWavetable : AnalysisBase {

    *new { |numPartials, server|
        ^super.new(numPartials, 0).initAnalysis(numPartials)
    }

    *analyse { |server, srcBuf, numPartials=32, windowSize=1024, hopSize, action|
        ^super.new(numPartials, 0).initAnalysis(numPartials)
            .analyse(server, srcBuf, numPartials, windowSize, hopSize, action)
    }

    analyse { |server, srcBuf, argNumPartials=32, argWindowSize=1024, argHopSize, action|
        var magBuf_   = Buffer(server);
        var phaseBuf_ = Buffer(server);

        numPartials = argNumPartials;
        windowSize  = argWindowSize;
        hopSize     = argHopSize ?? { argWindowSize / 2 };
        sampleRate  = server.sampleRate;

        FluidBufSTFT.processBlocking(server, srcBuf,
            magnitude:  magBuf_,
            phase:      phaseBuf_,
            windowSize: argWindowSize,
            hopSize:    hopSize,
        );

        magBuf_.loadToFloatArray(action: { |mags|
            phaseBuf_.loadToFloatArray(action: { |phasesRaw|
                var magFrames    = mags.clump(magBuf_.numChannels);
                var phaseFrames_ = phasesRaw.clump(phaseBuf_.numChannels);
                var initPhases;

                // pick bins with highest variance across frames
                var binVariance = Array.fill(magFrames[0].size, { |i|
                    magFrames.collect { |frame| frame[i] }.variance
                });
                var topBins = binVariance.collect { |v, bin| [v, bin] }
                    .sort { |a, b| a[0] > b[0] }
                    .keep(argNumPartials)
                    .collect { |pair| pair[1] };

                freqFrames = magFrames.collect {
                    topBins.collect { |bin| bin * sampleRate / argWindowSize }
                };

                ampFrames = magFrames.collect { |frame|
                    topBins.collect { |bin| frame[bin] }
                };

                // use raw STFT phase for first frame seed
                initPhases  = topBins.collect { |bin| phaseFrames_[0][bin] };
                phaseFrames    = this.prAccumPhases(freqFrames, initPhases, hopSize);

                numFrames = magFrames.size;

                magBuf_.free;
                phaseBuf_.free;

                action.value(this);
            });
        });

        ^this
    }

}


// =============================================================================
// AdditiveSines : AnalysisBase
// Analyses a source buffer with FluidBufSineFeature for proper partial tracking.
// Produces cleaner resynthesis than AdditiveWavetable for harmonic/tonal sounds.
// Also runs onset detection with FluidBufOnsetSlice for slice-based playback.
//
// Usage:
//   AdditiveSines.analyse(s, srcBuf, numPartials: 32, action: { |wt|
//       ~wt = wt;
//       ~wt.loadBuffers(s);
//   });
//   ~wt.readPhase(Phasor.ar(0, 1/s.sampleRate, 0, 1), sliceIdx: 0).oscBank
// =============================================================================

AdditiveSines : AnalysisBase {

    var <>slices, <>numSlices, <>sliceBuf;

    *new { |numPartials, server|
        ^super.new(numPartials, 0).initAnalysis(numPartials)
    }

    *analyse { |server, srcBuf, numPartials=32, windowSize=1024, hopSize,
                detectionThreshold= -60, slicerThreshold=0.5, order=0, action|
        ^super.new(numPartials, 0).initAnalysis(numPartials)
            .analyse(server, srcBuf, numPartials, windowSize, hopSize,
                detectionThreshold, slicerThreshold, order, action)
    }

    analyse { |server, srcBuf, argNumPartials=32, argWindowSize=1024, argHopSize,
               argDetectionThreshold= -60, argSlicerThreshold=0.5, order=0, action|
        var freqBuf_   = Buffer(server);
        var magBuf_    = Buffer(server);
        var onsetBuf_  = Buffer(server);

        numPartials = argNumPartials;
        windowSize  = argWindowSize;
        hopSize     = argHopSize ?? { argWindowSize / 2 };
        sampleRate  = server.sampleRate;

        FluidBufSineFeature.processBlocking(server, srcBuf,
            frequency:          freqBuf_,
            magnitude:          magBuf_,
            numPeaks:           argNumPartials,
            windowSize:         argWindowSize,
            hopSize:            hopSize,
            detectionThreshold: argDetectionThreshold,
            order: order
        );

        FluidBufOnsetSlice.processBlocking(server, srcBuf,
            indices:   onsetBuf_,
            threshold: argSlicerThreshold,
        );

        freqBuf_.loadToFloatArray(action: { |freqsRaw|
            magBuf_.loadToFloatArray(action: { |magsRaw|
                onsetBuf_.loadToFloatArray(action: { |onsets|
                    var numChan       = srcBuf.numChannels;
                    var rawFreqFrames = freqsRaw.clump(argNumPartials * numChan).collect { |frame|
                        frame.keep(argNumPartials)
                    };
                    var rawMagFrames  = magsRaw.clump(argNumPartials * numChan).collect { |frame|
                        frame.keep(argNumPartials)
                    };
                    var initPhases;

                    freqFrames = rawFreqFrames;
                    ampFrames  = rawMagFrames;

                    initPhases  = 0 ! argNumPartials;
                    phaseFrames = this.prAccumPhases(freqFrames, initPhases, hopSize);

                    // store slices as adjacent pairs of frame indices
                    slices = [];
                    onsets.doAdjacentPairs { |start, end|
                        slices = slices.add([
                            (start / hopSize).asInteger,
                            (end   / hopSize).asInteger
                        ]);
                    };
                    numSlices = slices.size;
                    numFrames = rawFreqFrames.size;

                    freqBuf_.free;
                    magBuf_.free;
                    onsetBuf_.free;

                    action.value(this);
                });
            });
        });

        ^this
    }

    getSlice { |idx|
        ^slices.wrapAt(idx)
    }

    pGetSlice { |generator|
        ^Pcollect({ |i| this.getSlice(i).asRef }, generator);
    }

}


// =============================================================================
// AdditiveReader : AdditiveChain
// UGen-side reader for AnalysisBase subclasses.
// Takes buffer numbers as args so no global reference is needed in the SynthDef.
// All AdditiveChain processing methods are inherited and work as normal.
//
// Usage:
//   SynthDef(\test, { |out=0, numPartials=32|
//       var bufs   = \wt.kr(0 ! 4);  // [freqBuf, ampBuf, phaseBuf, hopSize]
//       var reader = AdditiveReader(bufs[0], bufs[1], bufs[2], bufs[3], numPartials);
//       var sig    = reader
//           .readPhase(wPhase, startFrame: start, endFrame: end)
//           .stiffString(0)
//           .oscBank;
//       Out.ar(out, sig);
//   }).add;
//
//   Synth(\test, [\wt, ~wt, \numPartials, ~wt.numPartials]);
// =============================================================================

AdditiveReader : AdditiveChain {

    var <>readerFreqBuf, <>readerAmpBuf, <>readerPhaseBuf, <>readerHopSize;

    *new { |freqBuf, ampBuf, phaseBuf, hopSize, numPartials|
        ^super.new(numPartials, 0).initReader(freqBuf, ampBuf, phaseBuf, hopSize, numPartials)
    }

    initReader { |argFreqBuf, argAmpBuf, argPhaseBuf, argHopSize, argNumPartials|
        readerFreqBuf  = argFreqBuf;
        readerAmpBuf   = argAmpBuf;
        readerPhaseBuf = argPhaseBuf;
        readerHopSize  = argHopSize;
        numPartials    = argNumPartials;
        ^this
    }

    readFrame { |frame|
        freqs  = BufRd.ar(numPartials, readerFreqBuf,  frame);
        amps   = BufRd.ar(numPartials, readerAmpBuf,   frame);
        phases = BufRd.ar(numPartials, readerPhaseBuf, frame);
        ^this
    }

    readPhase { |phase, startFrame=0, endFrame|
        var end   = endFrame ?? { BufFrames.kr(readerFreqBuf) };
        var frame = phase.linlin(0, 1, startFrame, end);
        freqs  = BufRd.ar(numPartials, readerFreqBuf,  frame, interpolation: 4);
        amps   = BufRd.ar(numPartials, readerAmpBuf,   frame, interpolation: 4);
        phases = BufRd.ar(numPartials, readerPhaseBuf, frame, interpolation: 4);
        ^this
    }

}