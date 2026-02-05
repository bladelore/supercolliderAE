(
var initChain = { |numPartials, freq|
    (
        freq: freq,
        numPartials: numPartials,
        ratios: (1..numPartials),
        amps: 1 ! numPartials,
    )
};

var addLimiter = { |chain|
    var nyquist = SampleRate.ir * 0.5;
    var fadeStart = nyquist - 2000;
    var limiter = (1 - (chain[\freqs] - fadeStart) / 1000).clip(0, 1);
    chain[\amps] = chain[\amps] * limiter;
    chain;
};

var makeStretchedHarmonicSeries = { |chain, inharmonicity|
    chain[\freqs] = chain[\freq] * chain[\ratios] * (1 + (inharmonicity * chain[\ratios] * chain[\ratios])).sqrt;
    chain;
};

// RAZOR waveforms

// var pulseToSaw = {|amount, tilt, lvl, shift|
//     var dP = shift.ratiomidi;
//     var idx = chain[\ratios] - 1;
//     var mod2 = idx % 2;
//     var amountLvl = amount.linlin(0,1, 1.333, 1);
//     lvl = lvl.dbamp;
//     shift = 1/shift;

//     spreadIndices = ((idx / 2).floor * 2);
//     spreadIndices = (idx - spreadIndices).clip(0,1).linlin(0,1,-1,0);
//     partialPitchScaled = partialPitchScaled * spreadIndices;





// }

// RAZOR Dissonance
var partialDetune = {|chain, amount=0.1, partial_select=2, mode=0|
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
var stiffString = {|chain, amount=0.5|
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

var centroid = {|chain, amount, targetFreq|
    var newRatio;
    var partialPitch = chain[\freqs];
    var pitchArr = targetFreq ! chain[\numPartials];
    newRatio = amount.linlin(0, 1, partialPitch, pitchArr);    
    chain[\freqs] = newRatio;
    chain;
};

//not sure about this one, too many magic numbers
var reverse = {|chain, amount|
    var newRatio;
    amount = 1 - amount;
    newRatio = amount.linlin(0, 1, chain[\ratios].ratiomidi, (321.ratiomidi - 320.ratiomidi) ! chain[\numPartials]).min(200).midiratio;
    newRatio = newRatio * chain[\freqs];
    chain[\freqs] = newRatio;
    //TODO amps
    chain;
};

//RAZOR Stereo
//the same(?) as pan2.ar(sig, line).sum but for the sake of completeness:
var panLaw = {|in, pos|
    var left = in * (4 - (1 - pos)) * (1 - pos) * 0.333333;
    var right = in * (4 - (1 + pos)) * (1 + pos) * 0.333333;
    [left, right];
};

//in all examples ramp is the normalised selection of 0-n partial indices 
var simplePan = {|sig, chain, amount, ramp|
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
    panLaw.(sig, line);
};

var autoPan = {|sig, chain, amount, ramp, saw, cycles|
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
    panLaw.(sig, line);
};

var stereoSpread = {|sig, chain, amount, ramp, saw, cycles|
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
    // spreadIndices = ((idx / 2).floor * 2);
    // spreadIndices = (idx - spreadIndices).clip(0,1).linlin(0,1,-1,1);
    spreadIndices= (idx % 2).linlin(0,1,-1,1);
    partialPitchScaled = partialPitchScaled * spreadIndices;

    x0 = end; y0 = amount; m0 = 1/length.max(12); x=partialPitch;
    line = ((x - x0 * m0 + y0) * rampIndices) + centerIndices;
    line = line * partialPitchScaled;
    panLaw.(sig, line);
};

var air = {|sig, chain, amount, speed, min, max|

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

//notch
var notchFilter = { |chain, notchFreq, notchWidth|
    var freqs = chain[\freqs];
    var amps = chain[\amps];
    
    // Compute attenuation factor based on frequency proximity to notchFreq
    amps = amps.collect { |amp, i|
        var dist = (freqs[i] - notchFreq).abs;
        var attenuation = 1.0 - exp(dist.neg / notchWidth);
        amp * attenuation;
    };
    
    chain[\amps] = amps;
    chain;
};

{
    var chain, sig;
    chain = initChain.(60, 80);
    chain = makeStretchedHarmonicSeries.(chain, 0);

    //use one of these
    chain = partialDetune.(chain, amount: 0.01, partial_select: 2, mode: 1);
    // chain = stiffString.(chain, amount: 0.5);
    // chain = centroid.(chain, amount: SinOsc.ar(0.05).unipolar, targetFreq: 500);
    // chain = reverse.(chain, amount: SinOsc.ar(0.05).unipolar);
    chain = notchFilter.(chain, SinOsc.ar(0.5).linlin(-1,1,50,1000), 1000);
    chain = addLimiter.(chain);

    sig = SinOsc.ar(
        freq: chain[\freqs],
        phase: ({ Rand(0, 2pi) } ! chain[\numPartials]),
        mul: chain[\amps]
    );
    
    //use one of these
    
    //simple
    // sig = simplePan.(sig, chain, amount: SinOsc.ar(1).unipolar, ramp: 1);
    
    //these two are quite similar
    // sig = autoPan.(sig, chain, amount: SinOsc.ar(1).unipolar, ramp: SinOsc.ar(3).unipolar , saw: LFSaw.ar(0.5).unipolar, cycles: SinOsc.ar(0.1).unipolar);
    sig = stereoSpread.(sig, chain, amount: SinOsc.ar(1).unipolar, ramp: SinOsc.ar(1).unipolar, saw: LFSaw.ar(0.5).unipolar, cycles: 0.9);
    
    //the best one...
    // sig = air.(sig, chain, amount: 0.5, speed: 0.8, min: 0.1, max: 0.9);

    // sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);
    sig = sig * -30.dbamp;
}.play;
)

1 - (3 % 2)