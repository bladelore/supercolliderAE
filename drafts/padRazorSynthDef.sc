
(
var chain = ~initHarmonicsChain.(harmonics: 2, sidebands: 2, freq: 60);
chain.postln;
chain.asArray.postln;
)

[1,2,3].size

(
    SynthDef(\padRazor, {
        var sig;
        var ampDrift;
        var chain = \chain.kr([[1, 2, 3, 4], [1, 1, 1, 1], [0, 1, 0, 1], [1, 1, 2, 2], 60, 4, 2]);
        // var chain = \chain.kr(1!7);
        // var num_partials = chain[5];

        chain = (
            ratios: chain[0],
            amps: chain[1],
            sidebandIdx: chain[2],
            harmonicIdx: chain[3],
            freq: chain[4],
            numPartials: chain[5],
            sidebands: chain[6],  
        );

        chain = ~padSynthDistribution.(
            chain, 
            harmonicRatio: \harmonicRatio.kr(1),
            bw: \bw.kr(5000),
            bwScale: \bwScale.kr(1),
            bwSkew:  \bwSkew.kr(0),
            stretch: \stretch.kr(1),
            windowSkew: \windowSkew.kr(0.5)
        );
    
        chain = ~addLimiter.(chain);

        chain[\freqs]=chain[\freqs]+1e-4;
    
        sig = SinOsc.ar(
            freq: chain[\freqs],
            // phase: num_partials,
            mul: chain[\amps]
        );
        // sig = ~stereoSpread.(sig, chain, amount: SinOsc.ar(0.5).unipolar, ramp: 1, saw: LFNoise2.ar(3), cycles: 1);
        // sig = ~simplePan.(sig, chain, amount: SinOsc.ar(0.2).unipolar, ramp: 1);
        sig = ~air.(
            sig, 
            chain, 
            amount: \air_amount.kr(1), 
            speed: \air_speed.kr(1), 
            min: \min.kr(1), 
            max: \max.kr(0.9);
        );

        // sig = Balance2.ar(sig[0], sig[1]).sum;

        sig = sig * -15.dbamp;

        sig = Compander.ar(sig, sig,
            thresh: 0.5,
            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );

        sig=sig.sanitize;
                
        sig = sig * \gain.kr(-20).dbamp;
        sig = sig * \amp.kr(0.4);
        Out.ar(\out.kr(0), sig);
    }).add;
)

x.free;

(
var chain = ~initHarmonicsChain.(harmonics: 4, sidebands: 4, freq: 60);
// x.free;
x = Synth(\padRazor, [chain: chain]);
// x = Synth(\padRazor);

)