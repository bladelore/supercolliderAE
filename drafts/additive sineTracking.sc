(
{
var sig, chain, file, buf, ampDrift;
var lfos, combOffset, combDensity, combPeak, combSkew, warpSpectrum, inharmonicity, freq;
var harmonicRatio, ampScale, ampSkew, stretch, oddLevel, evenLevel;
var partialDrift, partialDriftFreq, partialDriftMD;
var phaseMD;

// file = "/Users/aelazary/Desktop/Samples etc./spirographshr/Video by spirographshr [C9X6OXxIIXK].wav";
file = "/Users/aelazary/Desktop/Samples etc./tape recs/tape in 0002 [2024-04-29 164022].aif";
// file = "/Users/aelazary/Desktop/Samples etc./Prism samples/Audio 0002 [2024-12-19 184628].aif";
// file = "/Users/aelazary/Desktop/Samples etc./Prism samples/Audio 0001 [2024-12-19 184620].aif";
// file = "/Users/aelazary/Desktop/Samples etc./NEW sample lib/BELLS/Rostov The Great. Chimes. Sysoy (on 2 edges). Egoryevsky zvon-pCcLC_ojzHQ.wav";
// file = "/Users/aelazary/Desktop/Samples etc./NEW sample lib/BELLS/Ringing the Peace Bell-F0kitWohMbo.wav";

buf = Buffer.readChannel(s, file, channels: [0]);

//dietcv modulation for comb filter
lfos = 6.collect{ |i|
    SinOsc.kr(\modMF.kr(0.1, spec: ControlSpec(0.1, 3)), Rand(0, 2pi));
};

combOffset = \combOffset.kr(0.8, spec: ControlSpec(0, 1));
combOffset = combOffset * (2 ** (lfos[0] * \combOffsetMD.kr(1, spec: ControlSpec(0, 2))));

combDensity = \combDensity.kr(0.1, spec: ControlSpec(0, 1));
combDensity = combDensity * (2 ** (lfos[1] * \combDensityMD.kr(0.1, spec: ControlSpec(0, 2))));

combPeak = \combPeak.kr(1, spec: ControlSpec(1, 10));
combPeak = combPeak * (2 ** (lfos[2] * \combPeakMD.kr(1, spec: ControlSpec(0, 2))));

combSkew = \combSkew.kr(0.5, spec: ControlSpec(0.01, 0.99));
combSkew = combSkew * (2 ** (lfos[3] * \combSkewMD.kr(1, spec: ControlSpec(0, 2))));

warpSpectrum = \warpSpec.kr(0.5, spec: ControlSpec(0, 1));
warpSpectrum = warpSpectrum * (2 ** (lfos[4] * \warpSpecMD.kr(0.1, spec: ControlSpec(0, 2))));

//generator funcs
//freq does nothing here
chain = ~initChain.(numPartials: 50, freq: 440);
//order 0 or 1
chain = ~extractSines.(chain, buf, freqLag: 0.01, ampLag: 0.1, order: 1, transpose: 0);
//mod funcs
// chain = warpFrequencies.(chain, warpSpectrum);
// chain = partialDetune.(chain, amount: SinOsc.ar(0.05).linlin(-1,1,0.01, 0.3), partial_select: SinOsc.ar(0.05).linlin(-1,1,2, 8), mode: -1);
// chain = stiffString.(chain, 0.2);
chain = ~addCombFilter.(chain, combOffset, combDensity, combSkew, combPeak);
// chain = centroid.(chain, amount: SinOsc.ar(0.05).unipolar, targetFreq: 30);
chain = ~notchFilter.(chain, SinOsc.ar(0.5).linlin(-1,1, 15000, 1000), 1000);
// chain = generateFormants.(chain, [
//     ( \freq: LFNoise1.ar(1).range(400, 600), \bandwidth: 200, \strength: 1.0 ),
//     ( \freq: LFNoise1.ar(2).range(1400, 1600), \bandwidth: 100, \strength: 0.8 ),
//     ( \freq: LFNoise1.ar(1.5).range(2400, 2600), \bandwidth: 120, \strength: 0.6 )
// ]);
chain = ~addLimiter.(chain);

sig = SinOsc.ar(
    freq: chain[\freqs],
    phase: ({ Rand(0, 2pi) } ! chain[\numPartials]),
    mul: chain[\amps]
);

//one of these
// sig = simplePan.(sig, chain, amount: SinOsc.ar(1).unipolar, ramp: 1);
// sig = autoPan.(sig, chain, amount: SinOsc.ar(1).unipolar, ramp: SinOsc.ar(3).unipolar , saw: LFSaw.ar(0.5), cycles: SinOsc.ar(0.1).unipolar);
// sig = stereoSpread.(sig, chain, amount: SinOsc.ar(0.5).unipolar, ramp: 0.5, saw: SinOsc.ar(3).unipolar, cycles: 1);
sig = ~air.(sig, chain, amount: 1, speed: 0.5, min: 0.1, max: 0.9);
// sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);

sig = sig * -30.dbamp;
}.play;
)

(
    {
        var chain, sig;
        var ampDrift;
        chain = ~initChain.(60, 50);
        // chain = pulseWidth.(chain, width: 0, tilt: 12, lvl: 12, shift: 1);
        // chain = makeStretchedHarmonicSeries.(chain, 0);
    
        chain = ~initHarmonicsChain.(harmonics: 9, sidebands: 5, freq: 60);
        chain = ~padSynthDistribution.(
            chain, 
            harmonicRatio: 1,
            bw: 1,
            bwScale: 1,
            bwSkew:  0,
            stretch: 1,
            windowSkew: 0.5
        );
    
        //use one of these
        // chain = ~partialDetune.(chain, amount: 0.01, partial_select: 2, mode: 1);
        // chain = ~stiffString.(chain, amount: 0.1);
        // chain = ~centroid.(chain, amount: SinOsc.ar(0.05).unipolar, targetFreq: 500);
        // chain = ~reverse.(chain, amount: 0.01);
        chain = ~notchFilter.(chain, SinOsc.ar(0.5).linlin(-1,1,50,1000), 2000);
        chain = ~addLimiter.(chain);
    
        ampDrift = LFNoise2.ar(2 ! chain[\numPartials]) * 0.1;
    
        sig = SinOsc.ar(
            freq: chain[\freqs],
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]),
            mul: chain[\amps]
        );
        
        //use one of these
        
        //simple
        // sig = ~simplePan.(sig, chain, amount: SinOsc.ar(1).unipolar, ramp: 1);
        
        //these two are quite similar
        // sig = ~autoPan.(sig, chain, amount: SinOsc.ar(1).unipolar, ramp: SinOsc.ar(3).unipolar , saw: LFSaw.ar(0.5).unipolar, cycles: SinOsc.ar(0.1).unipolar);
        // sig = ~stereoSpread.(sig, chain, amount: SinOsc.ar(0.5).unipolar, ramp: 0.5, saw: SinOsc.ar(0.9).unipolar, cycles: 1);
        
        //the best one...
        // sig = ~air.(sig, chain, amount: 0.5, speed: 0.75, min: 0.1, max: 0.9);
        sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);
        sig = sig * -30.dbamp;
    }.play;
    )