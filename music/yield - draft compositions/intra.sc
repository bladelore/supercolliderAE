Server.killAll()

(
    ~sliceBuf_L = Dictionary();
    ~sliceBuf_R = Dictionary();

    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./contact mics/bow mic.wav", ~sliceBuf_L, 0.5, \centroid, chans: 0);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./contact mics/bow mic.wav", ~sliceBuf_R, 0.5, \centroid, chans: 1);

    ~prism = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Prism samples/Audio 0002 [2024-12-19 184628].aif", ~prism, 0.1, \crest);

    ~drumloop = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Drum Loops/Silent Hill Origins/The Healer/H177_dirty_rev_60bpm.wav", ~drumloop, 0.25, \crest);
)

t = TempoClock.new(185/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

(

    SynthDef(\sineTracker_fx, {|in|
        var sig, chain, file, src, follower;
        var combOffset, combDensity, combPeak, combSkew, warpSpectrum, inharmonicity, freq;
        var partialDrift, partialDriftFreq, partialDriftMD, phaseMD;
        var ampDrift;

        var fbIn;
        
        //get fb
        fbIn = LocalIn.ar(2) * \feedback.kr(0);
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
            fftSize: 512, 
            hopSize: -1
        );
        //mod funcs
        chain = ~partialDetune.(chain, amount: SinOsc.ar(0.05).linlin(-1,1,0.01, 0.3), partial_select: SinOsc.ar(0.05).linlin(-1,1,2, 8), mode: -1);
        // chain = ~lpFilter.(chain, freq: 10000, qFactor: 2);
        // chain = ~notchFilter.(chain, SinOsc.ar(0.5).linlin(-1,1, 15000, 1000), 1000);
        // chain = ~hpFilter.(chain, freq: 100, qFactor: 8);
        // chain = ~lpFilter.(chain, freq: 8000, qFactor: 8);
        // chain = ~morphFilter.(chain, follower.linlin(0,1, 15000, 1000), order: 2, morph: SinOsc.ar(0.3).unipolar);

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

        // sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
        sig = sig * \gain.kr(0).dbamp;
        sig = sig * \amp.kr(1);
        Out.ar(\out.kr(0), sig);

    }).add;
)


(

    Pdef(\cut,
        Pbind(
            \instrument, \segPlayer,
            // \amp, 1,
            // \atk, 0.01,
            \amp, Pkey(\groupdelta).linexp(0, 1, 2, 0.5),
            \rel, Pkey(\dur) * 0.5,
            // \rel, Pkey(\groupdelta).linlin(0, 1, 0.1, 0.5),
            \curve, Pkey(\groupdelta).linlin(0,1, -4,0),
            \rate, 2,
            \oneshot, 1,
            \gain, -6,
            
            // \sliceStart, 200,

            // \sliceStart, 149,
            
            // \sliceStart, 100,

            // \sliceStart, 64,

            \sliceStart, 5,
            
            \pitchMix, 0,
            \pitchRatio, 2,
            \windowSize, 0.01,
            \pitchDispersion, 0.01,
            \timeDispersion, 0.02,
            \out, [~mainout, ~sineTracker],              
        )
    );

    Pdef(\cut_L,
        Pbind(
            \slice, ~pGetSlice.((Pseries(1, 1, inf).wrap(0, 64) + Pkey(\sliceStart)), ~sliceBuf_L), 
            \buf, ~sliceBuf_L.at(\file),
            // \pan, -1,
            \pan, ~pmodenv.(Pseq([-1, 1], inf), 8, 1, \sine),
        )
        // <>
        // Pdef(\cut)

    );

    Pdef(\cut_R,
        Pbind(
            \slice, ~pGetSlice.((Pseries(1, 1, inf).wrap(0, 64) + Pkey(\sliceStart)), ~sliceBuf_R), 
            \buf, ~sliceBuf_R.at(\file),
            // \pan, 1,
            \pan, ~pmodenv.(Pseq([1, -1], inf), 8, 1, \sine),
        )
        // <>
        // Pdef(\cut)

    );


    Pdef(\kick, 
        Pbind(
            \instrument, \fmKick2,
            \freq, Pseq([50, 50, 50, 55], inf),
            \atk, 0.04,
            // \dec, Pkey(\groupdelta).lincurve(0, 1, 0.2, 0.1),
            \dec, 0.5,
            // \rel, 0.01,
            \fb, Pkey(\groupdelta).linlin(0,1,0.4,0.2),
            // \fb, 0.2,
            // \fb, Pseq([0, 0.1, 0], inf).stutter(3),
            \pulseWidth, Pkey(\groupdelta) + 0.5,
            \index, (Pkey(\groupdelta).lincurve(1, 0, 3, 4) * 0.4).stutter(4),
            // \index, 1.5,
            \ratio, Pkey(\groupdelta).linlin(0,1,2,4),
            // \ratio, 4,
            \sweep, Pseq([8, 4, 8], inf),
            \spread, 10,
            \noise, 0,
            // \noise, Pseq([0, 2, 0], inf),
            \drive, 30,
            \feedback, 1,
            // \feedback, ((1-Pkey(\groupdelta)).linlin(0,1,3,4)).stutter(4),
            \fbmod, 0,
            \lofreq, 500.0,
            \lodb, -6.0,
            \midfreq, 1200.0,
            \middb, -12.0,
            \hifreq, 7000.0,
            \hidb, 10.0,
            \gain, -15.0,
            \pan, 0.0,
            \amp, 1,
            // \amp, Pkey(\groupdelta).lincurve(0, 1, 1, 0.1),
            \out, [~mainout, ~kick_out]
        )
    );

    Pdef(\snare, 
        Pbind(
            \instrument, \rim1,
            // \gate, 1.0,
            \freq, 50.0,
            \atk, 0.01,
            \dec, 0.3,
            \fb, 1.5,
            \index, 8.0,
            \ratio, 13.0,
            \sweep, 8.0,
            \spread, 3.0,
            \noise, 1.0,
            \roomsize, 3.0, 
            \reverbtime, 5.0,
            \gain, -8.0,
            \pan, 0.0,
            // \amp, Pkey(\groupdelta).linlin(0, 1, 1, 0.3),
            // \amp, 0
        )
    );

    Pdef(\prism, Pbind(
        \instrument, \segPlayer,
        \amp, 1,
        \buf, ~prism.at(\file),
        // \dur, 1,
        \sliceStart, 102,
        \sliceStutter, 24,
        \slice, ~pGetSlice.(Pseries(0, 1, inf).wrap(0, 32).stutter(Pkey(\sliceStutter)) + Pkey(\sliceStart), ~prism),
        //\pan, 0,
        \atk, 0.1,
        \rel, Pkey(\dur) * 1,
        \oneshot, 1,
        // \curve, Pkey(\groupdelta).linlin(0,1, -4,0),
        // \rel, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 1, 2, 0.75), Pkey(\dec), 1),
        // \curve, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, -8, 0), Pkey(\dec), 1),
        \rate, 1,
        \gain, -6,
        // \gain, ~pmodenv.(Pkey(\groupdelta).lincurve(0, 1, 9, 3, -4), Pkey(\dec), 1),
        //pitchshifter
        \pitchMix, 0,
        \pitchRatio, 2,
        // \windowSize, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.01, 0.25), Pkey(\rel), 1, \sine),
        // \pitchDispersion, ~pmodenv.(Pkey(\groupdelta).linlin(0, 1, 0.1, 0.01), Pkey(\rel), 1, \linear),
        \timeDispersion, 0.01,
    )
    );
)

(
    ~bufA = Buffer.alloc(s, 512);
    ~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
    ~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
    ~fftSize = 512;
    ~analysisFX = Array.fill(2, {Buffer.alloc(s, ~fftSize)});
)

(
    ~specBuff=Dictionary();
    ~specBuff2=Dictionary();
    ~specBuff3=Dictionary();
    // ~makeSpec.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/mother and daughter singing o magnum mysterium-sfLDOVcK7nU.wav", ~specBuff, 16384, 2)
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./spannerGuitar/spannerDrumGuitar-2.wav", ~specBuff, 16384, 2);
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./spannerGuitar/spannerDrumGuitar-3.wav", ~specBuff2, 16384, 2);
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./Prism samples/Audio 0002 [2024-12-19 184628].aif", ~specBuff3, 16384, 2);

)

~serumVst = VSTPluginController(Synth(\vsti)).open("Serum");
// ~serumVst.savePreset("combBass");
~serumVst.loadPreset("combBass");
~serumVst.editor

(
    ~busArr = [
        ~vocoder_out=Bus.audio(s,2),
        ~fftStretchLive_out=Bus.audio(s,2),
        ~reverb_out=Bus.audio(s,2),
        ~sineTracker_out=Bus.audio(s,2),

        ~break_out=Bus.audio(s,2),
        ~break2_out=Bus.audio(s,2),
        ~kick_out=Bus.audio(s,2),
        ~serum_out=Bus.audio(s,2),
        ~physmod_out=Bus.audio(s,2),
        ~droneB_out=Bus.audio(s,2)

    ]
)

(
    ~recorders = ~recordBuses.value(
        ~busArr,
        Platform.recordingsDir +/+ "intra1/%.wav"
    );
)

(
x.stop;
t = TempoClock.new(190/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});
x = {
    var reverb, sineTracker, break, break2, kick, fftStretch, serum, droneA, droneB, droneC, physmod, morph, filter;

    //fx
    ~serumVst = VSTPluginController(Synth(\vsti, [\out, [~serum_out]]) ).open("Serum");
    0.5.wait;
    ~serumVst.loadPreset("combBass");
    0.5.wait;
    
    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1], inf),
            PlaceAll([4, 4, 4, 4], inf)
        )
    );

    Pdef(\cut,
        Pbind(
            \instrument, \segPlayer,
            \amp, 1,
            // \atk, 0.01,
            \amp, Pkey(\groupdelta).linexp(0, 1, 2, 0.5),
            \rel, Pkey(\dur) * 0.25,
            // \rel, Pkey(\groupdelta).linlin(0, 1, 0.1, 0.5),
            \curve, Pkey(\groupdelta).linlin(0,1, -4,0),
            \rate, 2,
            \oneshot, 1,
            \gain, 0,
        
            \sliceStart, 149,
            
            \pitchMix, 0.4,
            \pitchRatio, 2,
            \windowSize, 0.01,
            \pitchDispersion, 0.01,
            \timeDispersion, 0.02,
        )
    );

    break = (
        Pbind(\out, [~sineTracker, ~mainout, ~fftStretchLive, ~miVerb, ~convolve_A, ~break_out]) <>
        Ppar([
            Pdef(\cut_L) <> Pdef(\cut), 
            Pdef(\cut_R) <> Pdef(\cut)
        ]) <> Pdef(\p1)

    ).play(t, quant:[1, 0]);
    
    4.wait;
    
    //////////////////
    \a.postln;

    Pbindef(\cut, \sliceStart, Pstep(Pseq([149, 100], inf), 4, inf)).quant([1,0]);
    Pbindef(\cut, \rel, Pkey(\dur) * 0.25).quant([1,0]);
    // Pbindef(\cut, \rel, Pstep(Pseq([0.1, 0.25], inf), 4, inf)).quant([1,0]);
    // Pbindef(\cut, \rate, Pstep(Pseq([1, 2], inf), 6, inf)).quant([1,0]);

    sineTracker = (
        Pdef(\sinetracker)
        <>
        Pbind(
            \feedback, ~pmodenv.(Pseq([0, 0.5], inf), 8, 1, \sine),
            \env_atk, 0.01,
            \env_rel, 0.1,
            \freqLag, 0.1,
            \ampLag, 0.1,
            \order, 0,
            \transpose, -24.0,
            \pan, 0.0,
            \gain, 0.0,
            \out, [~mainout, ~miVerb, ~sineTracker_out]
        )
    ).play(t, quant:[1, 0]);

    reverb = (
        Pbind(
            \time, 0.01,
            \damp, 0.9,
            \hp, 0,
            \freeze, 0,
            \diff, 0.9,
            \gain, -6,
            \out, [~mainout, ~reverb_out]
        )
        <>
        Pdef(\miVerb)
    ).play(t);  

    Pdef(\kp1,
        ~makeSubdivision.(
            PlaceAll([1, 0.5, 1, 0.5, 1], inf),
            PlaceAll([1, 1, 1, 2], inf)
        )
    );

    kick = (
        Pdef(\kick) <>
        ~filterBeat.(key: Pkey(\groupcount), beat:[2], reject: 1, mod: 2) <>
        ~filterBeat.(key: Pkey(\cyclecount), beat:[2], reject: 1, mod: 2) <>
        Pdef(\kp1)
    ).play(t, quant:[1, 0]);

    64.wait;

    //////////////////
    \b.postln;

    droneC = Pmono(\fftStretch_magAbove_mono,
        \amp, ~pmodenv.(Pseq([0.5, 1],inf), Pseq([4, 2, 4, 2], inf), 1, \sine),
        \gain, 0,
        \buf, ~specBuff3.at(\file),
        \analysis, [~specBuff3.at(\analysis)],
        \fftSize, ~specBuff3.at(\fftSize),
        \rate, 0.1,
        \pos, 0.3,
        // \pos, ~pmodenv.(Pseq([0.2, 0.8],inf), Pseq([4, 8, 4], inf), curve: \sine),
        // \pos, 0.8,
        \len, 0.1,
        \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
        \out, [~miVerb]
    ).play(t);

    Pbindef(\cut, \rel, Pkey(\dur) * 0.25).quant([1,0]);
    Pbindef(\cut, \pitchMix, 0.4).quant([1,0]);

    fftStretch = (
        Pdef(\fftStretchLive)
        <>
        Pbind(
            \buf, ~bufA,
            \amp, 1,
            \analysis, [~analysisFX],
            \fftSize, ~fftSize,
            \recRate, 1,
            // \recRate, 1,
            \len, 0,
            \thresh, 0.5,
            \remove, 2,
            \rate, 1,
            \pos, 0,
            \overdub, 0.1,
            \gain, -6,
            \dur, 0.1,
            \out, [~mainout, ~fftStretchLive_out]
        )
    ).play(t);

    Pbindef(\cut, \sliceStart, 149);

    kick.stop;
    kick = (
        Pdef(\kick) <>
        // ~filterBeat.(key: Pkey(\groupcount), beat:[2], reject: 1, mod: 2) <>
        ~filterBeat.(key: Pkey(\cyclecount), beat:[2], reject: 1, mod: 2) <>
        Pdef(\kp1)
    ).play(t, quant:[1, 0]);

    droneA = Pmono(\fftStretch_magAbove_mono,
        \amp, 1,
        \gain, 0,
        \buf, ~specBuff2.at(\file),
        \analysis, [~specBuff2.at(\analysis)],
        \fftSize, ~specBuff2.at(\fftSize),
        \rate, 0.1,
        \pos, 0.3,
        // \pos, ~pmodenv.(Pseq([0.2, 0.8],inf), Pseq([4, 8, 4], inf), curve: \sine),
        // \pos, 0.8,
        \len, 0.1,
        \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
        \out, [~convolve_B]
    ).play(t);

    physmod = Pdef(\fb1mod,
        Pbind(
            \instrument, \fb1,
            \amp, 1,
            \dec, Pkey(\dur) * 2,
            \feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
            \time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 100),inf), Pkey(\dec)),
            \damp, ~pmodenv.(Pseq([0.1, 1],inf), Pkey(\dec)),
            \exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec), 1, Pseq([\sine],inf)),
            \impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pkey(\dec)),
            \spont, ~pmodenv.(Pseq([60,1000],inf), Pkey(\dec)),
            \boost,  ~pmodenv.(Pseq([20000,200],inf), Pkey(\dec)),
            \restore, 5,
            \dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
            \rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
            \pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
            \gain, -6,
            \out, ~convolve_A
        ) <> Pdef(\p2)
    ).play(t);

    morph = Pbindf(
        Pdef(\morph),
        \gain, -14,
        \atk, 0,
        \rel, 100,
        \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([2], inf), 1, \sin),
        \out, [~mainout, ~miVerb, ~vocoder_out]
    ).play(t);
    
    64.wait;

    //////////
    \c.postln;

    // sineTracker.stop;

    kick.stop;

    Pdef(\kp2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 0.5, 1, 1, 1, 0.5, 1, 1], inf),
            PlaceAll([2, 1, 1, 1, 2, 1, 1, 1, [1, 0]], inf)
        )
    );

    kick = (
        Pdef(\kick) <>
        ~filterBeat.(key: Pkey(\groupcount), beat:[2], reject: 1, mod: 2) <>
        // ~filterBeat.(key: Pkey(\cyclecount), beat:[2], reject: 1, mod: 2) <>
        Pdef(\kp2)
    ).play(t, quant:[1, 0]);

    64.wait;

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1], inf),
            PlaceAll([4, 4, 4, 4], inf)
        )
    );

    // ///////////////////
    \d.postln;

    droneB = Pmono(\fftStretch_magAbove_mono,
        \amp, 1,
        \gain, 0,
        \buf, ~specBuff.at(\file),
        \analysis, [~specBuff.at(\analysis)],
        \fftSize, ~specBuff.at(\fftSize),
        \rate, 0.1,
        \pos, 0.3,
        // \pos, 0.8,
        \len, 0.1,
        \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
        \out, [~mainout, ~miVerb, ~droneB_out]
    ).play(t);

    //make more interesting.....

    fftStretch.stop;

    kick.stop;

    Pdef(\kp2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 0.5, 1, 0.5, 1, 0.5, 1, 1, 0.5], inf),
            PlaceAll([2, 1, 1, 1, 1, 1, 1, [1, 0], 1, 1], inf)
        )
    );

    kick = (
        Pdef(\kick) <>
        // ~filterBeat.(key: Pkey(\groupcount), beat:[2], reject: 1, mod: 2) <>
        ~filterBeat.(key: Pkey(\cyclecount), beat:[2, 5], reject: 1, mod: 6) <>
        Pdef(\kp2)
    ).play(t, quant:[1, 0]);

    Pbindef(\cut, 
        \rate, 1,
        \sliceStart, Pstep(Pseq([149, 200], inf), 4, inf),
        \rel, Pkey(\dur) * 0.25,
    );
    
    128.wait;

    //////////////////////////
    \e.postln;
    
    Pbindef(\cut, 
        \out, [~mainout, ~fftStretchLive],
        \rate, 1,
        \sliceStart, Pstep(Pseq([5, 25, 50, 75], inf), 4, inf),
        \rel, Pkey(\dur) * 0.25,
    );

    fftStretch = (
        Pdef(\fftStretchLive)
        <>
        Pbind(
            \buf, ~bufA,
            \amp, 1,
            \analysis, [~analysisFX],
            \fftSize, ~fftSize,
            \recRate, 1,
            // \recRate, 1,
            \len, 0,
            \thresh, 0.5,
            \remove, 2,
            \rate, 1,
            \pos, 0,
            \overdub, 0.1,
            \gain, -6,
            \dur, 0.1,
            \out, [~mainout, ~fftStretchLive_out]
        )
    ).play(t);

    128.wait;

    ////////////////////////
    \f.postln;

    Pbindef(\cut, 
        \rate, 1,
        // \slice, 149,
        \sliceStart, Pstep(Pseq([5, 25], inf), 4, inf),
        \rel, Pkey(\dur) * 0.25,
    );

    kick.stop;
    32.wait;

    ////////////////////
    \g.postln;

    physmod.stop;
    fftStretch.stop;
    sineTracker.stop;
    morph.stop;
    droneA.stop;
    droneB.stop;

    kick = (
        Pdef(\kick) <>
        // ~filterBeat.(key: Pkey(\groupcount), beat:[2], reject: 1, mod: 2) <>
        // ~filterBeat.(key: Pkey(\cyclecount), beat:[2], reject: 1, mod: 2) <>
        Pdef(\kp2)
    ).play(t, quant:[1, 0]);

    Pbindef(\cut, 
        \rate, 1,
        \amp, 1,
        \sliceStart, 5,
        // \sliceStart, 5,
        \rel, Pkey(\dur) * 0.25,
    );

    Pdef(\cut2,
        Pbind(
            \instrument, \segPlayer,
            \amp, 1,
            // \atk, 0.01,
            \amp, 1,
            \rel, Pkey(\dur) * 0.25,
            // \rel, Pkey(\groupdelta).linlin(0, 1, 0.1, 0.5),
            \curve, Pkey(\groupdelta).linlin(0,1, -4,0),
            \rate, 2,
            \oneshot, 1,
            \gain, -6,
        
            \sliceStart, 149,
            
            \pitchMix, 0,
            \pitchRatio, 2,
            \windowSize, 0.01,
            \pitchDispersion, 0.01,
            \timeDispersion, 0.02,
            \out, [~mainout, ~sineTracker],              
        )
    );

    break2 = (
        Pbind(\out, [~sineTracker, ~mainout, ~fftStretchLive, ~miVerb, ~break2_out]) <>
        Ppar([
            Pdef(\cut_L) <> Pdef(\cut2), 
            Pdef(\cut_R) <> Pdef(\cut2)
        ]) <> Pdef(\p1)

    ).play(t, quant:[1, 0]);

    serum =
    Pdef(\serum,
        Pbind(
            \type, \vst_midi,
            \vst, ~serumVst,
            \midicmd, \noteOn,
            \chan, 0,
            \midinote, Pseq([30, 40, 39, 34, 44, 30]-1, inf),
            \dur, 1/4,
            \amp, 1,
        )
    ).play(t, quant: [1, 0]);

    31.wait;

    kick.stop;
    break.stop;
    break2.stop;
    serum.stop;
    1.wait;
    
    Pbindef(\serum, \midinote, Pseq([30, 40, 39, 34, 44, 30], inf));

    kick.play(t, quant:[1,0]);

    break2 = (
        Pbind(\out, [~sineTracker, ~mainout, ~fftStretchLive, ~miVerb, ~break2_out]) <>
        Ppar([
            Pdef(\cut_L) <> Pdef(\cut2), 
            Pdef(\cut_R) <> Pdef(\cut2)
        ]) <> Pdef(\p1)

    ).play(t, quant:[1, 0]);

    break2.play(t, quant:[1,0]);

    serum.play(t, quant:[1,0]);

    31.wait;

    kick.stop;
    break.stop;
    break2.stop;
    serum.stop;
    1.wait;
    
    Pbindef(\serum, \midinote, Pseq([30, 40, 39, 34, 44, 30], inf));
    
    kick.play(t, quant:[1,0]);
    break.play(t, quant:[1,0]);
    
    break2 = (
        Pbind(\out, [~sineTracker, ~mainout, ~fftStretchLive, ~miVerb, ~break2_out]) <>
        Ppar([
            Pdef(\cut_L) <> Pdef(\cut2), 
            Pdef(\cut_R) <> Pdef(\cut2)
        ]) <> Pdef(\p1)

    ).play(t, quant:[1, 0]);

    serum.play(t, quant:[1,0]);

    31.wait;

    kick.stop;
    break.stop;
    break2.stop;
    serum.stop;
    1.wait;

    ////////////
    \h.postln;

    droneA = Pmono(\fftStretch_magAbove_mono,
        \amp, 1,
        \gain, 0,
        \buf, ~specBuff2.at(\file),
        \analysis, [~specBuff2.at(\analysis)],
        \fftSize, ~specBuff2.at(\fftSize),
        \rate, 0.1,
        \pos, 0.3,
        // \pos, ~pmodenv.(Pseq([0.2, 0.8],inf), Pseq([4, 8, 4], inf), curve: \sine),
        // \pos, 0.8,
        \len, 0.1,
        \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
        \out, [~convolve_B]
    ).play(t);

    physmod = Pdef(\fb1mod,
        Pbind(
            \instrument, \fb1,
            \amp, 1,
            \dec, Pkey(\dur) * 2,
            \feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
            \time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 100),inf), Pkey(\dec)),
            \damp, ~pmodenv.(Pseq([0.1, 1],inf), Pkey(\dec)),
            \exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec), 1, Pseq([\sine],inf)),
            \impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pkey(\dec)),
            \spont, ~pmodenv.(Pseq([60,1000],inf), Pkey(\dec)),
            \boost,  ~pmodenv.(Pseq([20000,200],inf), Pkey(\dec)),
            \restore, 5,
            \dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
            \rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
            \pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
            \gain, -6,
            \out, ~convolve_A
        ) <> Pdef(\p2)
    ).play(t);

    morph = Pbindf(
        Pdef(\morph),
        \gain, -14,
        \atk, 0,
        \rel, 100,
        \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([2], inf), 1, \sin),
        \out, [~mainout, ~miVerb, ~vocoder_out]
    ).play(t);
    
    Pbindef(\cut, \sliceStart, Pstep(Pseq([149, 100], inf), 4, inf)).quant([1,0]);

    serum =
    Pdef(\serum,
        Pbind(
            \type, \vst_midi,
            \vst, ~serumVst,
            \midicmd, \noteOn,
            \chan, 0,
            \midinote, Pseq([30, 40, 39, 34, 44, 30]-1, inf),
            \dur, 1/4,
            \amp, 1,
        )
    ).play(t, quant: [1, 0]);

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1], inf),
            PlaceAll([2, 2, 2, 2], inf)
        )
    );

    break = (
        Pbind(\out, [~sineTracker, ~mainout, ~fftStretchLive, ~miVerb, ~convolve_A, ~break_out]) <>
        Ppar([
            Pdef(\cut_L) <> Pdef(\cut), 
            Pdef(\cut_R) <> Pdef(\cut)
        ]) <> Pdef(\p1)
    ).play(t, quant:[1, 0]);

    Pbindef(\cut, \sliceStart, Pstep(Pseq([149, 100], inf), 4, inf)).quant([1,0]);
    Pbindef(\cut, \rel, Pkey(\dur) * 0.25).quant([1,0]);

    (   
        Pbind(\out, [~mainout, ~miVerb]) <>
        Pdef(\prism) <>
        // ~filterBeat.(key: Pkey(\groupcount), beat:[2], reject: 1, mod: 5) <>
        // ~filterBeat.(key: Pkey(\cyclecount), beat:[2], reject: 1, mod: 3) <>
        Pdef(\p2,
            ~makeSubdivision.(
                PlaceAll([1.5, 1.5, 1, 1], inf),
                PlaceAll([2, 2, 2, 2], inf)
            )
        );
    ).play(t);

    kick = (
        Pdef(\kick) <>
        ~filterBeat.(key: Pkey(\groupcount), beat:[2], reject: 1, mod: 2) <>
        ~filterBeat.(key: Pkey(\cyclecount), beat:[2], reject: 1, mod: 2) <>
        Pdef(\kp1)
    ).play(t, quant:[1, 0]);

    64.wait;
    //////////////
    \i.postln;

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1], inf),
            PlaceAll([2, 4, 2, 4], inf)
        )
    );
    
    kick.stop;
    kick = (
        Pdef(\kick) <>
        ~filterBeat.(key: Pkey(\groupcount), beat:[2], reject: 1, mod: 2) <>
        // ~filterBeat.(key: Pkey(\cyclecount), beat:[2], reject: 1, mod: 2) <>
        Pdef(\kp1)
    ).play(t, quant:[1, 0]);

    64.wait;

    \j.postln;

    break.stop;
    serum.stop;
    kick.stop;

}.fork(t);
)







(
// g.free;
// g = Group.new(RootNode(Server.default), \addToTail);

// Synth(\comp,
//     [
//         \ratio: 6,
//         \thresh, -40,
//         \atk, 0.1,
//         \rel, 1000,=
//         \makeup, 0,
//         \automakeup, 1
//     ],
//     target: g,
//     addAction: \addToTail,
// );


x.stop;
x = {

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1, [1.5, 1], 1], inf),
            PlaceAll([1, 2, 1, 2, 2], inf)
        )
    );

    // Pdef(\p2,
    //     ~makeSubdivision.(
    //         PlaceAll([0.5, 1, 1, 1, 1, 0.5, 0.5, 1], inf),
    //         PlaceAll([1, 2, 1, 2, 2], inf)
    //     )
    // );

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([1, 0.5, 1, 1, 0.5], inf),
            PlaceAll([1, 1, 1, 1], inf)
        )
    );

    // Pdef(\p2,
    //     ~makeSubdivision.(
    //         PlaceAll([1, 1, 0.5, 1, 1], inf),
    //         PlaceAll([2, 1, 1, 1], inf)
    //     )
    // );

    (
        // Pbind(\out, [~mainout, ~fftStretchLive])<>
        Pdef(\kick) <>
        ~filterBeat.(key: Pkey(\groupcount), beat:[2], reject: 1, mod: 2) <>
        ~filterBeat.(key: Pkey(\cyclecount), beat:[2], reject: 1, mod: 2) <>
        Pdef(\p2)
    ).play(t);

    (
        Pdef(\snare)
        <>
        Pbind(
            \dur, Pseq([Rest(3), 13], inf),
            \out, [~sineTracker, ~delay, ~fftStretchLive]
        )
    ).play(t);

    (
        Pdef(\delay) <> Pbind(\size, 0.5)
    ).play(t);

    (
        Pdef(\miVerb) 
        <>
        Pbind(
            \time, 0.01,
            \damp, 0.9,
            \hp, 0,
            \freeze, 0,
            \diff, 0.9,
            \gain, -12,
            \out, [~mainout]
        )
    ).play(t);

    (
        Pbind(\out, [~sineTracker, ~mainout, ~fftStretchLive, ~miVerb]) <>
        
        Ppar([Pdef(\cut_L) <> Pdef(\cut), Pdef(\cut_R)<> Pdef(\cut)])<>

        // Pbind(\pan, 0) <> Pdef(\cut_L)<>

        // ~filterBeat.(key: Pkey(\eventcount), beat:[1], reject: 1) <>
        // ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3], reject: 1) <>

        Pdef(\p1,
            ~makeSubdivision.(
                PlaceAll([1, 1, 1, 1], inf),
                PlaceAll([4, 4, 4, 4], inf)
            )
        );

    ).play(t);

    (
        Pdef(\sinetracker)
        <>
        Pbind(
            \feedback, ~pmodenv.(Pseq([0, 0.5], inf), 8, 1, \sine),
            \env_atk, 0.01,
            \env_rel, 0.1,
            \freqLag, 0.1,
            \ampLag, 0.1,
            \order, 1,
            \transpose, -24.0,
            \pan, 0.0,
            \gain, 0.0,
            \out, [~mainout, ~miVerb]
        )
    ).play(t);

    (
        Pdef(\fftStretchLive)
        <>
        Pbind(
            \buf, ~bufA,
            \amp, 1,
            \analysis, [~analysisFX],
            \fftSize, ~fftSize,
            \recRate, 0.25,
            // \recRate, 1,
            \len, 0,
            \thresh, 0.5,
            \remove, 2,
            \rate, 1,
            \pos, 0,
            \overdub, 0.1,
            \gain, -6,
            \dur, 0.1,
            \out, [~mainout]
        )
    ).play(t);

    Pmono(\fftStretch_magAbove_mono,
        \amp, 1,
        \gain, 0,
        \buf, ~specBuff.at(\file),
        \analysis, [~specBuff.at(\analysis)],
        \fftSize, ~specBuff.at(\fftSize),
        \rate, 0.1,
        \pos, 0.3,
        // \pos, 0.8,
        \len, 0.1,
        \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
        \out, [~miVerb]
    ).play(t);

    Pmono(\fftStretch_magAbove_mono,
        \amp, 1,
        \gain, 0,
        \buf, ~specBuff2.at(\file),
        \analysis, [~specBuff2.at(\analysis)],
        \fftSize, ~specBuff2.at(\fftSize),
        \rate, 0.1,
        \pos, 0.3,
        // \pos, ~pmodenv.(Pseq([0.2, 0.8],inf), Pseq([4, 8, 4], inf), curve: \sine),
        // \pos, 0.8,
        \len, 0.1,
        \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
        \out, [~convolve_B]
    ).play(t);

    Pdef(\fb1mod,
        Pbind(
            \instrument, \fb1,
            \amp, 1,
            \dec, Pkey(\dur) * 2,
            \feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8] * 0.5,inf), Pkey(\dec)),
            \time, ~pmodenv.(Pseq(([0.005, 0.001, 0.010] * 100),inf), Pkey(\dec)),
            \damp, ~pmodenv.(Pseq([0.1, 1],inf), Pkey(\dec)),
            \exciter, ~pmodenv.(Pwhite(1, 0, inf).lincurve(0, 1, 0, 1, -8), Pkey(\dec), 1, Pseq([\sine],inf)),
            \impulse, ~pmodenv.(Pwhite(20000, 200,inf), Pkey(\dec)),
            \spont, ~pmodenv.(Pseq([60,1000],inf), Pkey(\dec)),
            \boost,  ~pmodenv.(Pseq([20000,200],inf), Pkey(\dec)),
            \restore, 5,
            \dist,  ~pmodenv.(Pseq([16, 32],inf), Pkey(\dec)),
            \rev, ~pmodenv.(Pexprand(0.1, 4, inf), Pkey(\dec)),
            \pan, ~pmodenv.(Pwhite(-0.3, 0.3, inf), Pkey(\dec)),
            \gain, -6,
            \out, ~convolve_A
        ) <> Pdef(\p2)
    ).play(t);

    Pbindf(
        Pdef(\morph),
        \gain, -14,
        \atk, 0,
        \rel, 100,
        \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/2], inf), 1, \sin),
        // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1/3], inf), 1, \sin),
        // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([2, 1, 1], inf), 1, \sin),
        // \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([1.5], inf), 1, \sin),
        // \swap, 0,
        // \swap, 1,
        \out, [~mainout, ~miVerb]
    ).play(t)

    
}.fork(t);
)