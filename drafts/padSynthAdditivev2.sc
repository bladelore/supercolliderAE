(
var initHarmonicsChain = { |harmonics=10, sidebands=5, freq=80|
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

var hprofile = {|fi, bwi, windowSkew=0.5|
    var x = abs(fi - windowSkew) * 2;
    x = x / bwi;
    x = x * x;
    x = exp(x.neg);
    x;
};

var padSynthDistribution = { |chain, harmonicRatio=1, bw=1, bwScale=1, bwSkew=1, stretch=1, windowSkew=0.5|
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
    chain[\amps] = hprofile.(chain[\sidebandIdx].linlin(0, chain[\sidebands]-1, 0, 1), bwi, windowSkew);
};

var removeNyquistPartials = {|chain|
    var nyquestIdx = chain[\freqs].selectIndices({|item, i| (item >= (s.sampleRate)) || (item <= 0.0)});
    chain[\amps].putEach(nyquestIdx, 0);
    chain;
};

var addLimiter = { |chain|
    var nyquist = SampleRate.ir * 0.5;
    var fadeStart = nyquist - 2000;
    var limiter = (1 - (chain[\freqs] - fadeStart) / 1000).clip(0, 1);
    chain[\amps] = chain[\amps] * limiter;
    chain;
};

var evenOddMask = {|chain, oddLevel, evenLevel|
    chain[\amps] = chain[\amps].collect { |item, i| if(i.odd){ item * oddLevel; } { item * evenLevel; } };
    chain;
};

var evenOddHarmonicMask = {|chain, oddLevel, evenLevel|
    chain[\amps] = chain[\amps].collect { |item, i| if(i.odd){ item * oddLevel; } { item * evenLevel; } };
    chain;
};

{
var chain, sig, lfos, freqDrift, ampDrift;

chain = initHarmonicsChain.(harmonics: 15, sidebands: 9, freq: 60);
// chain = padSynthDistribution.(
//     chain, 
//     harmonicRatio: 0.75,
//     bw: 1000,
//     bwScale: LFNoise2.kr(2) * 1.5, 
//     bwSkew:  0,
//     stretch: 1,
//     windowSkew: LFNoise2.kr(2)
// );

chain = padSynthDistribution.(
    chain, 
    harmonicRatio: 1,
    bw: 1000,
    bwScale: 0.1,
    bwSkew:  0,
    stretch: 1,
    windowSkew: 1
);

chain[\amps].postln;

// chain = evenOddMask.(chain, LFNoise2.kr(3).unipolar, LFNoise2.kr(2).unipolar);
chain = addLimiter.(chain);

freqDrift = LFNoise2.ar(500 ! chain[\numPartials]) * 1;
ampDrift = LFNoise2.ar(2 ! chain[\numPartials]) * 0.25;

sig = SinOsc.ar(
    freq: chain[\freqs] + freqDrift,
    phase: ({ Rand(0, 2pi) } ! chain[\numPartials]),
    mul: chain[\amps]
);

sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);
sig = sig * -25.dbamp;
}.play;

)
