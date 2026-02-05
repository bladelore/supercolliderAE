(

    SynthDef(\sineTracker_fx, {|in|
        var sig, chain, file, src, follower;
        var combOffset, combDensity, combPeak, combSkew, warpSpectrum, inharmonicity, freq;
        var partialDrift, partialDriftFreq, partialDriftMD, phaseMD;
        var ampDrift;

        var fbIn;
        
        //get fb
        fbIn = LocalIn.ar(2) * \feedback.kr(0.5);
        src = InFeedback.ar(\inbus.kr(0), 2) + fbIn;
            
        follower = Amplitude.ar(src, \env_atk.kr(0.01), \env_rel.kr(0.1));
        //generator funcs
        chain = ~initChain.(numPartials: 50, freq: 440);
        //order 0 or 1
        chain = ~extractSines.(
            chain, 
            src, 
            freqLag: \freqLag.kr(0.01), 
            ampLag: \ampLag.kr(0.1), 
            order: \order.kr(1), 
            transpose: \transpose.kr(0), 
            winSize: 2048, 
            fftSize: 4096, 
            hopSize: -1
        );

        //mod funcs
        // chain = ~partialDetune.(chain, amount: SinOsc.ar(0.05).linlin(-1,1,0.01, 0.3), partial_select: SinOsc.ar(0.05).linlin(-1,1,2, 8), mode: -1);
        // chain = ~lpFilter.(chain, freq: 10000, qFactor: 2);
        // chain = ~notchFilter.(chain, SinOsc.ar(0.5).linlin(-1,1, 15000, 1000), 1000);
        // chain = ~hpFilter.(chain, freq: 100, qFactor: 8);
        // chain = ~lpFilter.(chain, freq: 8000, qFactor: 8);

        chain = ~morphFilter.(chain, follower.linlin(0,1, 15000, 1000), order: 2, morph: SinOsc.ar(0.3).unipolar);
        chain = ~addLimiter.(chain);
        
        sig = SinOsc.ar(
            freq: chain[\freqs],
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]) * SinOsc.ar(0.1).unipolar,
            mul: chain[\amps]
        );
        
        //one of these
        // sig = ~simplePan.(sig, chain, amount: SinOsc.ar(1).unipolar, ramp: 1);
        sig = ~autoPan.(sig, chain, amount: SinOsc.ar(1).unipolar, ramp: SinOsc.ar(3).unipolar , saw: LFSaw.ar(0.5), cycles: SinOsc.ar(0.1).unipolar);
        // sig = ~stereoSpread.(sig, chain, amount: SinOsc.ar(0.5).unipolar, ramp: 0.5, saw: SinOsc.ar(3).unipolar, cycles: 1);
        // sig = ~air.(sig, chain, amount: 0.8, speed: 1, min: 0, max: 0.9);
        // sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);
        
        sig = Compander.ar(sig, sig,
            thresh: 0.5,
            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );
        
        sig=sig.softclip;
        sig = sig.sanitize;
        //send fb
        LocalOut.ar(sig);

        sig = HPF.ar(sig, 100);

        sig = Compander.ar(sig, sig,
            thresh: -6.dbamp,
            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );

        sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
        sig = sig * \gain.kr(0).dbamp;
        sig = sig * \amp.kr(1);
        Out.ar(\out.kr(0), sig);
    }).add;
)

(
SynthDef(\sineTracker_fx, {|in|
    var sig, chain, file, src, follower;
    var combOffset, combDensity, combPeak, combSkew, warpSpectrum, inharmonicity, freq;
    var partialDrift, partialDriftFreq, partialDriftMD, phaseMD;
    var ampDrift;

    var fbIn;
    
    //get fb
    fbIn = LocalIn.ar(2) * \feedback.kr(0.5);
    src = InFeedback.ar(\inbus.kr(0), 2) + fbIn;
        
    follower = Amplitude.ar(src, \env_atk.kr(0.01), \env_rel.kr(0.1));
    //generator funcs
    chain = ~initChain.(numPartials: 50, freq: 440);
    //order 0 or 1
    chain = ~extractSines_smooth.(
        chain, 
        src, 
        freqLag: \freqLag.kr(0.01), 
        ampLag: \ampLag.kr(0.1), 
        order: \order.kr(1), 
        transpose: \transpose.kr(0), 
        winSize: 2048, 
        fftSize: 4096, 
        hopSize: -1
    );
    //mod funcs
    // chain = ~partialDetune.(chain, amount: SinOsc.ar(0.05).linlin(-1,1,0.01, 0.3), partial_select: SinOsc.ar(0.05).linlin(-1,1,2, 8), mode: -1);
    // chain = ~lpFilter.(chain, freq: 10000, qFactor: 2);
    // chain = ~notchFilter.(chain, SinOsc.ar(0.5).linlin(-1,1, 15000, 1000), 1000);
    // chain = ~lpFilter.(chain, freq: 10000, qFactor: 8);

    // chain = ~morphFilter.(chain, SinOsc.ar(0.5).linlin(-1,1, 15000, 1000), order: 2, morph: SinOsc.ar(0.3).unipolar);
    chain = ~addLimiter.(chain);
    
    sig = SinOsc.ar(
        freq: chain[\freqs],
        phase: ({ Rand(0, 2pi) } ! chain[\numPartials]) * SinOsc.ar(0.1).unipolar,
        mul: chain[\amps]
    );
    
    //one of these
    // sig = ~simplePan.(sig, chain, amount: SinOsc.ar(1).unipolar, ramp: 1);
    // sig = ~autoPan.(sig, chain, amount: SinOsc.ar(1).unipolar, ramp: SinOsc.ar(3).unipolar , saw: LFSaw.ar(0.5), cycles: SinOsc.ar(0.1).unipolar);
    // sig = ~stereoSpread.(sig, chain, amount: SinOsc.ar(0.5).unipolar, ramp: 0.5, saw: SinOsc.ar(3).unipolar, cycles: 1);
    sig = ~air.(sig, chain, amount: 0.2, speed: 1, min: 0.1, max: 0.9);
    // sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);
    
    sig = Compander.ar(sig, sig,
        thresh: 0.5,
        slopeBelow: 1,
        slopeAbove: 0.1,
        clampTime:  0.01,
        relaxTime:  0.01
    );
    
    sig=sig.softclip;
    sig = sig.sanitize;
    //send fb
    LocalOut.ar(sig);

    // sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
    sig = sig * \gain.kr(0).dbamp;
    sig = sig * \amp.kr(1);
    Out.ar(\out.kr(0), sig);

}).add;
)

// sp.par(
//     Pdef(\sinetracker)
//     <>
//     Pbind(
//         \feedback, 0.9,
//         \env_atk, 0.01,
//         \env_rel, 0.5,
//         \freqLag, 0.1,
//         \ampLag, 0.1,
//         \order, 0,
//         \transpose, -12.0,
//         \pan, 0.0,
//         \gain, -12.0,
//     )
// );