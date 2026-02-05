(
~initChain = { |numPartials, freq|
    (
        freq: freq,
        numPartials: numPartials,
        ratios: (1..numPartials),
        amps: 1 ! numPartials,
    )
};

~addLimiter = { |chain|
    var nyquist = SampleRate.ir * 0.5;
    var fadeStart = nyquist - 2000;
    var limiter = (1 - (chain[\freqs] - fadeStart) / 1000).clip(0, 1);
    chain[\amps] = chain[\amps] * limiter;
    chain;
};

~makeStretchedHarmonicSeries = { |chain, inharmonicity|
    chain[\freqs] = chain[\freq] * chain[\ratios] * (1 + (inharmonicity * chain[\ratios] * chain[\ratios])).sqrt;
    chain;
};

~evenOddMask = {|chain, oddLevel, evenLevel|
    chain[\amps] = chain[\amps].collect { |item, i| if(i.odd){ item * oddLevel; } { item * evenLevel; } };
    chain;
};

~transferFunc = { |phase, skew|
    Select.kr(phase > skew, [
        0.5 * phase / skew,
        0.5 * (1 + ((phase - skew) / (1 - skew)))
    ]);
};

~warpFrequencies = { |chain, warpPoint|
    var normalizedFreqs = (chain[\freqs] / chain[\freqs][chain[\numPartials] - 1]).clip(0, 1);
    var warpedFreqs = ~transferFunc.(normalizedFreqs, 1 - warpPoint);
    chain[\freqs] = warpedFreqs * chain[\freqs][chain[\numPartials] - 1];
    chain;
};

~addSpectralTilt = { |chain, tiltPerOctave|
    chain[\amps] = chain[\amps] * (chain[\ratios].log2 * tiltPerOctave).dbamp;
    chain;
};

// RAZOR waveforms
~pulseWidth = {|chain, width, tilt, lvl, shift|
    var deltaPitch = shift.ratiomidi;
    var idx = chain[\ratios];
    var pitch = chain[\freq].cpsmidi;
    var partialPitch = (chain[\ratios] * chain[\freq]).cpsmidi;
    var overallTilt;
    
    lvl = lvl.dbamp;
    shift = 1/shift;

    width = (width - 1) * -0.25 * shift * idx;
    width = width.wrap(0, 2pi);
    width = sin(width).abs;
    width = lvl.dbamp * 1.333 * width;

    overallTilt = (partialPitch - (pitch + deltaPitch)) * (tilt - 6) * (1/12);
    overallTilt = overallTilt.dbamp;

    chain[\amps] = overallTilt * width;
    chain[\freqs] = chain[\freq] ! chain[\numPartials];
    chain;
};

// ~pulseToSaw = {|amount, tilt, lvl, shift|
//     var dP = shift.ratiomidi;
//     var idx = chain[\ratios] - 1;
//     var mod2 = idx.wrap(0, 1);
//     var amountLvl = amount.linlin(0,1, 1.333, 1);
//     lvl = lvl.dbamp;
//     shift = 1/shift;
//     spreadIndices = ((idx / 2).floor * 2);
//     spreadIndices = (idx - spreadIndices).clip(0,1).linlin(0,1,-1,0);
//     partialPitchScaled = partialPitchScaled * spreadIndices;
// }

// RAZOR Dissonance
~partialDetune = {|chain, amount=0.1, partial_select=2, mode=0|
    var idx = chain[\ratios] - 1;
    var freq = chain[\freq];
    var detune = ((50 / freq) * amount) - mode;
    var whichIdx = ((idx / partial_select).floor * partial_select);
    var isDetuned = (idx - whichIdx).clip(0,1);
    detune = detune * isDetuned;
    // detune = (chain[\freq] * (chain[\ratios] + detune));
    detune = (chain[\freqs] ? 0) + (chain[\freq] * detune);
    chain[\freqs] = detune;
    chain;
};

//not sure about this one
~stiffString = {|chain, amount=0.5|
    var scaledAmt = amount.linlin(0, 1, -140, 60);
    var db2AF = scaledAmt.dbamp;
    var idx = chain[\ratios];
    var newRatio = (idx.squared * db2AF) + 1;
    newRatio = ((log2(newRatio) * 0.5) ** 2) * idx + 1;

    //unsure what this should be, unclear in the reaktor patch
    // chain[\freqs] = newRatio * chain[\freq];
    chain[\freqs] = (chain[\freqs] ? 0) + (chain[\freq] * newRatio);
    chain;
};

~centroid = {|chain, amount, targetFreq|
    var newRatio;
    var partialPitch = chain[\freqs];
    var pitchArr = targetFreq ! chain[\numPartials];
    newRatio = amount.linlin(0, 1, partialPitch, pitchArr);    
    chain[\freqs] = newRatio;
    chain;
};

//not sure about this one, too many magic numbers
~reverse = {|chain, amount=1|
    var amountRev = 1-amount;
    var reversePitch, newRatio, ampAmount, ampIndices;
    reversePitch = amountRev.linlin(0, 1, chain[\ratios].ratiomidi, (321.ratiomidi - 320.ratiomidi) ! chain[\numPartials]);
    newRatio = reversePitch.midiratio;
    
    // ampAmount = (1 - amount) * (320.ratiomidi);
    // ampAmount.postln;
    // ampIndices = (ampAmount / reversePitch).clip(0,1).floor;
    // reversePitch.postln;
    // ampIndices.postln;

    chain[\freqs] = newRatio * chain[\freqs];
    //TODO amps
    chain;
};

//RAZOR Stereo
//the same(?) as pan2.ar(sig, line).sum but for the sake of completeness:
~panLaw = {|in, pos|
    var left = in * (4 - (1 - pos)) * (1 - pos) * 0.333333;
    var right = in * (4 - (1 + pos)) * (1 + pos) * 0.333333;
    [left, right];
};

//in all examples ramp is the normalised selection of 0-n partial indices 
~simplePan = {|sig, chain, amount, ramp|
    var partialPitch, pitch, start, length, end;
    var rampIndices, centerIndices;
    var x0, y0, m0, x, line;

    partialPitch = chain[\freqs].cpsmidi;
    pitch = chain[\freq].cpsmidi;
    start = pitch;
    length = ramp.linlin(0, 1, chain[\freqs][0], chain[\freqs][chain[\numPartials]-1]).cpsmidi;
    end = length;
    
    rampIndices = 1 - (partialPitch.floor / end.floor).floor.clip(0,1);
    centerIndices = (0.5 - rampIndices).clip(0,1);

    x0 = end; y0 = 1; m0 = 1/length.max(12); x=partialPitch;
    line = ((x - x0 * m0 + y0) * rampIndices * amount) + centerIndices;
    line = line.linlin(0,1,-1,1).lag(0.1, 0.1);
    ~panLaw.(sig, line);
};

~autoPan = {|sig, chain, amount, ramp, saw, cycles|
    var partialPitch, partialPitchScaled, pitch, start, length, end;
    var rampIndices, centerIndices;
    var x0, y0, m0, x, line;
    //scaling
    partialPitch = chain[\freqs].cpsmidi;
    partialPitchScaled = partialPitch - 80;
    pitch = chain[\freq].cpsmidi;
    cycles = ((1-cycles)*30.neg).dbamp * cycles;
    partialPitchScaled = partialPitchScaled * cycles;
    saw = saw * 0.5.neg;
    partialPitchScaled = partialPitchScaled + saw;
    partialPitchScaled = partialPitchScaled - partialPitchScaled.collect(_.round);
    partialPitchScaled = partialPitchScaled * (8 - (partialPitchScaled.abs * 16));
    //panned indices
    start = pitch;
    length = ramp.linlin(0, 1, chain[\freqs][0], chain[\freqs][chain[\numPartials]-1]).cpsmidi;
    end = length;
    rampIndices = 1 - (partialPitch.floor / end).floor.clip(0,1);
    centerIndices = (1 - rampIndices);
    //ramp
    x0 = end; y0 = amount; m0 = 1/length.max(12); x=partialPitch;
    line = ((x - x0 * m0 + y0) * rampIndices) + centerIndices;
    line = line * partialPitchScaled;
    line = line.lag(0.1, 0.1);
    ~panLaw.(sig, line);
};

~stereoSpread = {|sig, chain, amount, ramp, saw, cycles|
    var idx, partialPitch, partialPitchScaled, pitch, start, length, end;
    var rampIndices, centerIndices, spreadIndices;
    var x0, y0, m0, x, line;

    idx = chain[\ratios];
    partialPitch = chain[\freqs].cpsmidi;
    partialPitchScaled = partialPitch - 80;
    pitch = chain[\freq].cpsmidi;
    cycles = ((1-cycles)*30.neg).dbamp * cycles;
    partialPitchScaled = partialPitchScaled * cycles;
    saw = saw * 0.5.neg;
    partialPitchScaled = partialPitchScaled + saw;

    partialPitchScaled = partialPitchScaled - partialPitchScaled.collect(_.round);
    partialPitchScaled = partialPitchScaled * (8 - (partialPitchScaled.abs * 16));
    
    start = pitch;
    length = ramp.linlin(0, 1, chain[\freqs][0], chain[\freqs][chain[\numPartials]-1]).cpsmidi;
    end = length;
    rampIndices = 1 - (partialPitch.floor / end).floor.clip(0,1);
    centerIndices = (1 - rampIndices);

    //flip polarity of every second partial
    spreadIndices= idx.wrap(0,1).linlin(0,1,-1,1);
    partialPitchScaled = partialPitchScaled * spreadIndices;

    x0 = end; y0 = amount; m0 = 1/length.max(12); x=partialPitch;
    line = ((x - x0 * m0 + y0) * rampIndices) + centerIndices;
    line = line * partialPitchScaled;
    ~panLaw.(sig, line);
};

~air = {|sig, chain, amount, speed, min, max|

    var idx = chain[\ratios];
    var partialFreqs = chain[\freqs];
    var noise_L, noise_R;
    var idxAmp, rangeAmp, ampScale;
    var amtFade, amtMult;
    var pan, left, right;

    speed = speed.linlin(0,1,-200,0).midiratio;
    min = min.linlin(0,1,-70,40);
    max = max.linlin(0,1,-70,40);
    
    noise_L = partialFreqs * speed + 0.333;
    noise_R = partialFreqs * speed + 0.456;

    noise_L = LFBrownNoise2.ar(noise_L, 1, 1, 5);
    noise_R = LFBrownNoise2.ar(noise_R, 1, 1, 5);

    idxAmp = (chain[\amps] * (idx - 1)).max(0.0001).dbamp - max;
    rangeAmp = 1/(max - min).max(0.1).neg;

    ampScale = (idxAmp * rangeAmp).clip(0, 1) * amount;

    amtFade = (1 - ampScale);
    amtFade = (2 - amtFade) * amtFade;
    amtMult = (2 - ampScale) * ampScale;
    amtMult = amtMult * 2;

    noise_L = (noise_L * amtMult) + amtFade;
    noise_R = (noise_R * amtMult) + amtFade;
    [noise_L * sig, noise_R * sig];
};

//Filter
~notchFilter = { |chain, notchFreq, notchWidth|
    var freqs = chain[\freqs];
    var amps = chain[\amps];
    
    var dist = (freqs - notchFreq).abs;
    var attenuation = 1.0 - exp(dist.neg / notchWidth);
    
    chain[\amps] = amps * attenuation;
    chain;
};

~generateFormants = { |chain, formants|
    var freqs = chain[\freqs];
    var amps = chain[\amps];
    
    // Initialize the resulting amplitudes
    amps = amps.collect { |amp, i|
        var freq = freqs[i];
        // Sum contributions from all formants
        var formantContribution = formants.collect { |formant|
            var centerFreq = formant[\freq];
            var bandwidth = formant[\bandwidth];
            var strength = formant[\strength];
            // Gaussian distribution for smooth formant shape
            strength * exp(((freq - centerFreq).squared).neg / (2 * (bandwidth.squared)))
        }.sum;
        amp * formantContribution
    };
    
    chain[\amps] = amps;
    chain;
};

~raisedCos = { |phase, index|
    var cosine = cos(phase * 2pi);
    var raised = exp(index.abs * (cosine - 1));
    var hanning = 0.5 * (1 + cos(phase * 2pi));
    raised * hanning;
};

~addCombFilter = { |chain, combOffset, combDensity, combSkew, combPeak|
    var phase, warpedPhase;
    phase = chain[\freqs].log2 - chain[\freq].log2;
    phase = (phase * combDensity - combOffset).wrap(0, 1);
    warpedPhase = ~transferFunc.(phase, combSkew);
    chain[\amps] = chain[\amps] * ~raisedCos.(warpedPhase, combPeak);
    chain;
};

//padsynth
~initHarmonicsChain = { |harmonics=10, sidebands=5, freq=80|
    var numPartials = harmonics * sidebands;
    (
        freq: freq,
        numPartials: numPartials,
        ratios: (1..numPartials),
        amps: 1 ! numPartials,

        sidebands: sidebands,
        harmonicIdx: (1..harmonics).dupEach(sidebands),
        sidebandIdx: (0..sidebands - 1).wrapExtend(numPartials)
    )
};

~hprofile = {|fi, bwi, windowSkew=0.5|
    var x = abs(fi - windowSkew) * 2;
    x = x / bwi;
    x = x * x;
    x = exp(x.neg);
    x;
};

~padSynthDistribution = { |chain, harmonicRatio=1, bw=1, bwScale=1, bwSkew=1, stretch=1, windowSkew=0.5|
    //get harmonic integers
    var amps;
    var powN = pow(chain[\harmonicIdx], harmonicRatio / 2);
    var relF = (powN * ((1.0 + (powN - 1)) * stretch));
    var bw_Hz, bwi, fi;
    var idxOffset = (chain[\sidebands] / 2).floor;
    var partialIdx, freqOffset, subPartialFreq, subPartialAmp;
    
    // //harmonic frequency
    chain[\freqs] = (relF * chain[\freq]);
    bw_Hz = (pow(2, (bw / 1200) - bwSkew)) * chain[\freq] * pow(relF, bwScale);
    bwi = (1 / (chain[\harmonicIdx])) * bwScale;
    //for each harmonic, create n-1 sidebands with amplitudes of each sideband on gaussianish distribution
    freqOffset = bw_Hz / chain[\sidebands];
    partialIdx = chain[\sidebandIdx] - idxOffset;
    chain[\freqs] = chain[\freqs] + (partialIdx * freqOffset);
    chain[\amps] = ~hprofile.(chain[\sidebandIdx].linlin(0, chain[\sidebands]-1, 0, 1), bwi, windowSkew);
};

//analysis
~extractSines = {|chain, buf, freqLag, ampLag, order, transpose|
    var source = PlayBuf.ar(1, buf, loop: 1);
    
    var analysis = FluidSineFeature.kr(
        source, 
        order: order,
        numPeaks: chain[\numPartials], 
        maxNumPeaks: 50, 
        windowSize: 512, 
        fftSize: 4096,
        hopSize: 4,
    );
    
    transpose = transpose.midicps;

    chain[\freqs] = analysis[0].lag(freqLag, freqLag) + 0.00001 + transpose;
    chain[\amps] = analysis[1].lag(ampLag, ampLag);
    chain;
};
)