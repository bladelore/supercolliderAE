//slice buffers
(
    ~sliceBuf = Dictionary();
    ~sliceBuf2 = Dictionary();
    ~sliceBuf3 = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Missing Sounds 2016/04-Hobble_Break_126_PL_1.WAV", ~sliceBuf, 0.1, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/12011108amlampostwiresa33road.wav", ~sliceBuf2, 0.1, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/CityCountryMeworkumdrache.wav", ~sliceBuf3, 0.2, \crest);
)

//spectral sample buffers
(
    ~specBuff=Dictionary();
    ~specBuff2=Dictionary();
    // ~makeSpec.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/mother and daughter singing o magnum mysterium-sfLDOVcK7nU.wav", ~specBuff, 16384, 2)
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/backyard oct 11 2018.wav", ~specBuff, 16384, 2);
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./Field recs/Brush metal bowl small 2.wav", ~specBuff2, 16384, 2);
)

//instruments
(
    Pdef(\kick, 
        Pbind(
            // \dur, 1,
            \instrument, Prand([\fmPerc2, \fmPerc3], inf),
            \freq, Pseq([60.0], inf),
            \atk, 0.01,
            \dec, Pkey(\dur) * 0.5,
            \fb, 0.4,
            \index1, 1,
            \index2, 2,
            \ratio1, 0.5,
            \ratio2, 2,
            \drive, 0,
            \drivemix, 0,
            \sweep, 8.0,
            \spread, 20.0,
            \noise, 0.25,
            \feedback, Pkey(\cycledelta) * 1,
            \fbmod, 1,
            \pulseWidth, 1 - Pkey(\groupdelta).linlin(0,1, 0.1, 0.99),
            \lofreq, 500.0,
            \lodb, 10.0,
            \midfreq, 1200.0,
            \middb, 0.0,
            \hifreq, 7000.0,
            \hidb, 30.0,
            \gain, -23.0,
            \pan, 0.0,
            // \hpf, 
            \amp, Pkey(\groupdelta).linlin(0, 1, 1, 0.3),
            // \amp, 1,
            \out, [~mainout]
        )
    );

    Pdef(\cut1,
        Pbind(
            \instrument, \segPlayer,
            \amp, 1,
            \atk, 0.01,
            // \amp, Pkey(\groupdelta).linexp(0, 1, 1, 0.3),
            \rel, Pkey(\dur) * 0.5,
            \curve, Pkey(\groupdelta).linlin(0,1, -4,0),
            \buf, ~sliceBuf.at(\file),
            \rate, 1,
            \oneshot, 1,
            // \gain, -6,
            // \sliceStart, 12,
            // \sliceStart, 15,
            \sliceStart, 20,
            // \sliceStart, 27,
            // \sliceStart, 44,
            \slice, ~pGetSlice.((Pseries(1, 1, inf).wrap(0, 1) + Pkey(\sliceStart)), ~sliceBuf).stutter(4),
            \pan, ~pmodenv.(Pwhite(-0.5, 0.5, inf), Pkey(\dur)),
            \pitchMix, 0.5,
            // \pitchRatio, 2,
            \windowSize, 0.01,
            \pitchDispersion, 0.01,
            \timeDispersion, 0.05,
            \out, [~mainout],              
        )
    );

    Pdef(\cut2,
        Pbind(
            \instrument, \segPlayer,
            \amp, 1,
            // \amp, Pkey(\groupdelta).linexp(0, 1, 1, 0.3),
            \atk, 0.01,
            \rel, Pkey(\dur) * 0.5,
            \curve, Pkey(\groupdelta).linlin(0,1, -4,0),
            \buf, ~sliceBuf2.at(\file),
            \rate, 1,
            \oneshot, 1,
            \gain, 9,
            // \sliceStart, 128,
            \sliceStart, 300,
            \slice, ~pGetSlice.((Pseries(1, 1, inf).wrap(0, 3) + Pkey(\sliceStart)), ~sliceBuf2),
            \pitchMix, 0.5,
            \pitchRatio, 1,
            \windowSize, 0.05,
            \pitchDispersion, 0.01,
            \timeDispersion, 0.05,
            \out, [~mainout],              
        )
    );

    Pdef(\cut3,
        Pbind(
            \instrument, \segPlayer,
            \amp, 1,
            // \amp, Pkey(\groupdelta).linexp(0, 1, 0.4, 1),
            \atk, 0.01,
            \rel, Pkey(\dur) * 0.125,
            // \rel, Pkey(\dur) * 1,
            \curve, Pkey(\groupdelta).linlin(0,1, -4,0),
            \buf, ~sliceBuf3.at(\file),
            \rate, 1,
            \oneshot, 1,
            \gain, 9,
            \sliceStart, 0,
            \slice, ~pGetSlice.((Pseries(1, 1, inf).wrap(0, 12) + Pkey(\sliceStart)), ~sliceBuf3),
            \pitchMix, 0.5,
            \pitchRatio, 1,
            \windowSize, 0.05,
            \pitchDispersion, 0.01,
            \timeDispersion, 0.05,
            \out, [~mainout],              
        )
    );

    Pdef(\synth,
        Pbind(
            \scale, Scale.mixolydian,
            \dur, Pseq([1.5, 1.5, 1, 1, 1, 1, 1, 1, 1] * 0.5, inf),
            // \scale, Scale.minor,
            \root, 0, 
            \ctranspose, -2,
            // \mtranspose, Pstep(Pseq([0, 0, -0.5, -0.5], inf), 8),
            \degree, Pseq([3, 2, 1], inf),
            // \degree, Pseq(([5, 1, 2, 3, -2, -2, 1].dup(3) ++ [5, 2, 3, -1, 0, -1, 2].dup(3)).flat, inf),
            // \degree, Pseq(([3, 1, 2, 3, -2, -2, 1].dup(3) ++ [1, 2, 3, -1, 0, -1, 2].dup(3)).flat, inf),
            \octave, 4,
            \detune, 0.5,
            \envDepth, -0.5,
    
            \atk, 0.25,
            \dec, 0.4,
            \sus, 0.1,
            \rel, 1,
            
            \filterAtk, 0.1,
            \filterDec, 1,
            \pitchEnv, 1,
    
            \driftRate, 2.0,
            \drift, 1.0,
            \lfoShape, 1,
            \lfoFreq, 1,
    
            \lfoToWidth, 0.5,
            \lfoToShapeAmount, 0,
            \lfoToFilter, 1,
            \lfoToMorph, 0.5,
    
            \filter, 50.0,
            \filter2, 5000.0,
            // \filter2, ~pmodenv.(Pwhite(5000, 500), Pkey(\dur), 1, \sin),
            \width, 1,
            \shape, 1,
            \morph, 0.5,
            // \morph, ~pmodenv.(Pwhite(-1, 1), Pkey(\dur), 1, \sin),
            \gain, -16.0,
            \amp, 1.0,
            \pan, ~pmodenv.(Pwhite(-0.25, 0.25), Pkey(\dur), 1, \sin),
            // \pan, -0.7,
            \out, [~mainout],
            \instrument, \pulsePluck
        )
    );

    SynthDef(\samplePlayer, {|buf, loop=1|
        var sig, sample, pan, panned, numChans;
    
        numChans = buf.numChannels;
    
        sig = PlayBuf.ar(2, buf, loop: loop);
    
        pan = \pan.kr(0);
        panned = case
        {numChans == 1} {Pan2.ar(sig, pan)}
        {numChans == 2} {Balance2.ar(sig[0], sig[1], pan)}
        {numChans > 2} { var splay = Splay.ar(sig); Balance2.ar(splay[0], splay[1], pan); } ;
    
        sig = sig * \amp.kr(1);
        Out.ar(\out.kr(0), sig);
    }).add;
    
    ~introSample = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./radioaporee/020420241142PMwindrecambimicsairharp.wav");
    ~bowlSample = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./Field recs/Brush metal bowl small 2.wav");
)

//fft bufs
(
    ~bufA = Buffer.alloc(s, 512);
    ~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
    ~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
    ~fftSize = 512;
    ~analysisFX = Array.fill(2, {Buffer.alloc(s, ~fftSize)});
)

//sinetracker
(

    // ~quantizePartials = {}

    // ~centroid = {|chain, amount, scale|
    //     var newRatio;
    //     var partialPitch = chain[\freqs];
    //     var pitchArr = targetFreq ! chain[\numPartials];


    //     // newRatio = amount.linlin(0, 1, partialPitch, pitchArr);    
    //     chain[\freqs] = newRatio;
    //     chain;
    // };

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

        // sig = Balance2.ar(sig[0], sig[1], \pan.kr(0));
        sig = sig * \gain.kr(0).dbamp;
        sig = sig * \amp.kr(1);
        Out.ar(\out.kr(0), sig);

    }).add;
)

//serum
~serumVst = VSTPluginController(Synth(\vsti)).open("Serum");
~serumVst.loadPreset("distortedHit");
~serumVst.editor
// ~serumVst.savePreset("distortedHit2");

(
g.free;
g = Group.new(RootNode(Server.default), \addToTail);

Synth(\comp,
    [
        \ratio: 6,
        \thresh, -40,
        \atk, 0.1,
        \rel, 1000,
        \makeup, 0,
        \automakeup, 1
    ],
    target: g,
    addAction: \addToTail,
);

t = TempoClock.new(152/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});
x.stop;
x = {
    var sectionLength, reverb, sample, bowl, backyard, backyard2, stretch, kick, cut1, cut2, cut3, roar, morph, fftStretchLive, fftStretchLive2, synth, serum, grain;
    var verb, drum, filter, sineTracker;

    // load vst
    ~serumVst = VSTPluginController(Synth(\vsti)).open("Serum");
    0.5.wait;
    ~serumVst.loadPreset("distortedHit");
    0.5.wait;

    ~morphGroup = Group();
    roar = (
        Pbindf(
            Pdef(\roar),
            \dur, 0.01,
            \drive, 9.0,
            \tone, ~pmodenv.(Pseq([-0.99, 0.9], inf), 0.5),
            \toneFreq, 500.0,
            \toneComp, 1.0,
            \drywet, 0.8,
            \bias, 0,
            // \filterFreq, 6000,
            \filterLoHi, ~pmodenv.(Pseq([0, 0.75], inf), 1),
            \filterBP, 0,
            \filterRes, 0.3,
            \filterBW, 0.5,
            \filterPre, 1.0,
            \feedAmt, 7.0,
            \feedFreq, 50.0,
            \feedBW, 0.1,
            \feedDelay, 0.1,
            \feedGate, 0.05,
            \gain, -9.0,
            \amp, 1.0,
            \group, ~morphGroup,
            \out, [~mainout]
        )
    ).play(t);
    
    morph = (
        Pbind(
            \gain, 0,
            \atk, 10,
            \rel, 100,
            \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([1.5], inf), 1, \sine),
            \out, [~mainout, ~miVerb]
        ) <>
        Pdef(\morph)
    ).play(t);
  
    fftStretchLive =
        Pbindf(
            Pdef(\fftStretchLive),
            \buf, ~bufA,
            \amp, 1,
            \analysis, [~analysisFX],
            \fftSize, ~fftSize,
            \recRate, ~pmodenv.(Pseq([1.5, 0.25], inf), 0.5),
            \len, 0,
            \thresh, 10,
            \remove, 10,
            \rate, 1,
            \pos, 0,
            \overdub, 0,
            \gain, -12,
            \dur, 0.1,
            \out, [~mainout]
        );
    fftStretchLive = fftStretchLive.play(t);
    
    filter = 
		Pbindf(
			Pdef(\filter),
			\blend, ~pmodenv.(Pseq([-1, 1], inf), 1.5),
			// \freq, Pseg(Pseq([300, 800],inf), 4, 'lin' , inf),
            \freq, 500,
			\res, 0.5,
			\out, [~mainout]
		);
    filter = filter.play(t);
    
    reverb =
        Pbindf(
            Pdef(\miVerb),
            \time, 1,
            \damp, 0.99,
            \hp, 0.125,
            \freeze, 0,
            \diff, 0.1,
            \gain, 0,
            \out, ~mainout
        );
    reverb = reverb.play(t);

    sineTracker = 
        Pdef(\sinetracker)
        <>
        Pbind(
            \feedback, 0.8,
            \env_atk, 0.01,
            \env_rel, 0.1,
            \freqLag, 0.1,
            \ampLag, 0.01,
            \order, 0,
            \transpose, 0.0,
            \pan, 0.0,
            \gain, 0.0,
        );
    sineTracker = sineTracker.play(t);
    
    //////// START //////////////
    
    \intro.postln;
    sectionLength = 8;

    sample = PfadeIn(Pmono(\samplePlayer, \buf, ~introSample, \out, [~mainout, ~sineTracker]), 8);
    sample = sample.play(t);

    sectionLength.wait;

    //////////////////////
    sectionLength = 28;

    synth = 
        PfadeIn(
            Pbind(\out, [~convolve_B]) <>
            Pdef(\synth)
        , 28
    );
    synth = synth.play(t);
    
    backyard =
        PfadeIn(
            Pdef(\backyard,
                    Pmono(\fftStretch_magAbove_mono,
                        \dur, 0.1,
                        \amp, 1,
                        \gain, 16,
                        \buf, ~specBuff.at(\file),
                        \analysis, [~specBuff.at(\analysis)],
                        \fftSize, ~specBuff.at(\fftSize),
                        \rate, 0.01,

                        // \pos, 0.15,
                        // \pos, 0.25,
                        // \pos, 0.01,
                        // \pos, 0.55,
                        // \pos, 0.7,

                        \filter, 1,
                        \pos, Pstep(Pseq([0.7, 0.55], inf), 4),
                        \len, 0.2,
                        \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
                        \out, [~filter, ~miVerb]
                    )
                )
        , 20
    );
    backyard = backyard.play(t, quant: [16, 0]);

    sectionLength.wait;
    
    ///////////////////////////////////////////////////

    \a.postln;

    sectionLength = 24;

    //p1 intro phased
    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, Rest(4)], inf),
            PlaceAll([4, 2, 3], inf)
        )	
    );

    Pdef(\p1_transform,
        ~filterBeat.(key: Pkey(\eventcount), beat:[3, 1], reject: 1) <>
        ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [2, 4], skew: [1, -1], curve: \exp) <>
        Pdef(\p1)
    );

    cut2 = 
        Pbind(\out, [~roar]) <>
        Pdef(\cut2) <>
        Pdef(\p1_transform);
    cut2 = cut2.play(t);

    kick =
        Pbind(\out, [~roar, ~convolve_B]) <>
        Pdef(\kick)  <>
        ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 3) <>
        ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1] * 0.25, curve: \exp) <>
        Pdef(\p1);
    kick = kick.play(t);

    sectionLength.wait;

    //////////////////////////

    \a2.postln;
    sectionLength = 44;

    cut2.stop;
    kick.stop;
    
    Pdef(\p1_transform,
        ~filterBeat.(key: Pkey(\eventcount), beat:[3, 1], reject: 1) <>
        ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [2, 4], skew: [1, -1], curve: \exp) <>
        Pdef(\p1)
    );
    
    cut2 = 
        Pbind(\out, [~roar]) <>
        Pdef(\cut2) <>
        Pdef(\p1_transform);
    cut2 = cut2.play(t);

    kick = 
        Pbind(\out, [~roar]) <>
        Pdef(\kick)  <>
        ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 3) <>
        ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1] * 0.25, curve: \exp) <>
        Pdef(\p1);
    kick = kick.play(t);
    
    sectionLength.wait;
    // sp.suspend(cut1);
    cut2.stop;
    kick.stop;
    8.wait;

    ///////////////////////////////////////////////////

    \b.postln;
    sectionLength = 38;
    // cut2.stop;

    synth.stop;
    
    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1] * 1.5, inf),
            PlaceAll([4, 2, 3], inf)
        )	
    );

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1], inf),
            PlaceAll([4, 2, 3, 4], inf)
        )	
    );

    Pdef(\p1_transform,
        ~filterBeat.(key: Pkey(\eventcount), beat:[2], reject: 1) <>
        ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 4], skew: [0.75, -0.75], curve: \exp) <>
        Pdef(\p1)
    );

    serum =
        Pbind(
            \type, \vst_midi,
            \vst, ~serumVst, // the VSTPluginController instance
            \midicmd, \noteOn, // the default, can be omitted
            \chan, 0, // MIDI channel (default: 0)
            \midinote, 43,
            \dur, Pseq([1.5, Rest(19-1.5), 1.5, Rest(20-1.5)], inf),
            \amp, 2,
            \out, [~roar, ~miVerb]
            // \gain, 6
        );
    serum = serum.play(t);

    kick =
        Pbind(\out, [~roar, ~convolve_A]) <>
        Pdef(\kick)  <>
        ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 3) <>
        // ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1] * 0.25, curve: \exp) <>
        Pdef(\p1);
    kick = kick.play(t);

    cut1 =
        Pbind(\out, [~roar]) <>
        Pdef(\cut1) <>
        Pdef(\p1_transform);
    cut1 = cut1.play(t);

    Pbindef(\cut1, \sliceStart, Pseq([15, 12], inf).stutter(6));
    Pbindef(\cut1, \rel, 0.25);
    // Pbindef(\cut1, \sliceStart, 48);

    cut2 = 
        Pbind(\out, [~roar]) <>
        Pdef(\cut2) <>
        Pdef(\p1_transform);
    cut2 = cut2.play(t);

    cut3 = 
        Pbind(\out, [~roar, ~miVerb]) <>
        Pdef(\cut3) <>
        // ~filterBeat.(key: Pkey(\eventcount), beat:[3]) <>
        ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3], reject: 1) <>
        Pdef(\p2);
    cut3 = cut3.play(t);
    

    synth =
        Pbind(\out, [~roar, ~miVerb]) <>
        Pdef(\synth);
    synth = synth.play(t);

    sectionLength.wait;
    1.wait;

    // /////////////////////////////////////////////////////

    \c.postln;
    // sectionLength = 48;
    sectionLength = 68;
    morph.stop;
    cut1.stop;
    cut2.stop;
    cut3.stop;
    kick.stop;
    synth.stop;
    serum.stop;
    // backyard.stop;

    serum =
        Pbind(
            \type, \vst_midi,
            \vst, ~serumVst, // the VSTPluginController instance
            \midicmd, \noteOn, // the default, can be omitted
            \chan, 0, // MIDI channel (default: 0)
            \midinote, 43,
            \dur, Pseq([1.5, Rest(16-1.5)], inf),
            \amp, 2,
            \out, ~roar
        );
    serum = serum.play(t);
    
    morph =
        Pbind(
            \gain, 0,
            \atk, 10,
            \rel, 100,
            \swap, ~pmodenv.(Pseq([0.95, 0],inf), Pseq([1], inf), 1, \sine),
            // \swap, 0.5,
            \out, [~mainout]
        ) <>
        Pdef(\morph);
    morph = morph.play(t);

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1, 1.5, 1.5, 1], inf),
            PlaceAll([4, 2, 3, 2, 4, 4, 4], inf)
        )
    );

    Pdef(\p1_transform,
        ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3], reject: 1) <>
        // ~filterBeat.(key: Pkey(\eventcount), beat:[1, 4], reject: 1) <>
        ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1] * 0.25, curve: \exp) <>
        Pdef(\p1)
    );

    cut1 =
        Pbind(\out, [~roar]) <>
        // Pbind(\out, [~mainout]) <>
        Pdef(\cut1) <>
        Pdef(\p1_transform);
    cut1 = cut1.play(t);

    Pbindef(\cut1, \sliceStart, 20);

    cut2 = 
        Pbind(\out, [~roar]) <>
        Pdef(\cut2) <>
        Pdef(\p1_transform);
    cut2 = cut2.play(t);

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1], inf),
            PlaceAll([4, 4, 4], inf)
        )	
    );

    cut3 =
        Pbind(\out, [~fftStretchLive]) <>
        Pdef(\cut3) <>
        ~filterBeat.(key: Pkey(\cyclecount), beat:[2, 7, 14], reject: 1, mod: 16) <>
        ~filterBeat.(key: Pkey(\eventcount), beat:[3], reject: 1) <>
        Pdef(\p2);
    cut3 = cut3.play(t);
    
    Pbindef(\cut3, \rate, 2);

    kick = 
        // Pbind(\out, [~mainout]) <>
        Pbind(\out, [~roar]) <>
        Pdef(\kick)  <>
        ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 3) <>
        // ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1] * 0.25, curve: \exp) <>
        Pdef(\p1);
    kick = kick.play(t);

    synth =
        Pbind(\out, [~convolve_A, ~miVerb]) <>
        Pdef(\synth);
    synth = synth.play(t);

    bowl =
        Pbind(\out, [~convolve_B]) <>
        Pdef(\bowl,
            Pmono(\fftStretch_mono,
                \amp, 1,
                \gain, 0,
                \buf, ~specBuff2.at(\file),
                \analysis, [~specBuff2.at(\analysis)],
                \fftSize, ~specBuff2.at(\fftSize),
                \rate, 1,
                \pos, 0.7,
                \len, 0.1,
            )
        );
        
    bowl = bowl.play(t, quant:[32, 0]);
    
    backyard.stop;
    backyard2 = (
        Pbind(\out, [~filter, ~miVerb]) <>
        Pdef(\backyard,
                Pmono(\fftStretch_magAbove_mono,
                    \dur, 0.1,
                    \amp, 1,
                    \gain, 12,
                    \buf, ~specBuff.at(\file),
                    \analysis, [~specBuff.at(\analysis)],
                    \fftSize, ~specBuff.at(\fftSize),
                    \rate, 0.01,
                    \filter, 0.5,
                    \amp, 1,
                    \pos, Pstep(Pseq([0.46], inf), Pseq([16], inf)),
                    \len, 0.2,
                    \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
                )
            )
    ).play(t);

    sectionLength.wait;
    2.wait;

    // // // /////////////////////////////////////////////////////

    \d.postln;
    sectionLength = 56;
    
    serum.stop;
    serum =
        Pbind(
            \type, \vst_midi,
            \vst, ~serumVst, // the VSTPluginController instance
            \midicmd, \noteOn, // the default, can be omitted
            \chan, 0, // MIDI channel (default: 0)
            // \midinote, Pstep(Pseq([43, 44]), 16, inf),
            \midinote, 44,
            \dur, Pseq([1.5, Rest(16-1.5)] * 0.75, inf),
            \amp, 2,
            \out, ~roar
            // \gain, 6
        );
    serum = serum.play(t);
    
    // morph =
    //      Pbind(
    //          \gain, 0,
    //          \atk, 10,
    //          \rel, 100,
    //          \swap, ~pmodenv.(Pseq([1, 0],inf), Pseq([2], inf), 1, \sine),
    //          \out, [~mainout]
    //      ) <>
    //      Pdef(\morph);
    // morph = morph.play(t);

    morph.stop;
    morph = (
        Pbind(
            \gain, 0,
            \atk, 10,
            \rel, 100,
            \swap, 0,
            \out, [~mainout, ~miVerb]
        ) <>
        Pdef(\morph)
    ).play(t);
 
     Pdef(\p1,
         ~makeSubdivision.(
             PlaceAll([1, 1, 1, 1], inf),
             PlaceAll([4, 2, 3], inf)
         )	
     );
 
     Pdef(\p1_transform,
         ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3], reject: 1) <>
         // ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1] * 0.25, curve: \exp) <>
         // ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [2, 4], skew: [1, -1], curve: \exp) <>
         Pdef(\p1)
     );
    
    cut1.stop;
    cut1 = 
        //  Pbind(\out, [~mainout, ~sineTracker]) <>
         Pbind(\out, [~roar, ~sineTracker]) <>
         Pdef(\cut1) <>
         Pdef(\p1_transform);
    cut1 = cut1.play(t);
 
    Pbindef(\cut1, \sliceStart, Pseq([20], inf).stutter(6));
    
    cut2.stop;
    cut2 =
            Pbind(\out, [~roar]) <>
            Pdef(\cut2) <>
            Pdef(\p1_transform);
    cut2 = cut2.play(t);

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1], inf),
            PlaceAll([4, 2, 3], inf)
        )	
    );
    
    cut3.stop;
    cut3 =
        Pbind(\out, [~roar]) <>
        Pdef(\cut3) <>
        ~filterBeat.(key: Pkey(\eventcount), beat:[3], reject: 1) <>
        Pdef(\p2);
    cut3 = cut3.play(t);
    
    kick.stop;
    kick =
        Pbind(\out, [~roar, ~convolve_A]) <>
        Pdef(\kick)  <>
            ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 3) <>
            // ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1] * 0.25, curve: \exp) <>
            Pdef(\p1);
    kick = kick.play(t);
     
    synth.stop;
    synth =
        Pbind(\out, [~roar]) <>
        Pbind(\amp, ~pmodenv.(Pseq([1, 0.4], inf), 2)) <>
        Pdef(\synth);
    synth = synth.play(t);
 
    sectionLength.wait;
    1.wait;

    // /////////////////////////////////////////////////
        
    \e.postln;
    sectionLength = 64;
    synth.stop;
    // backyard2.stop;
    // bowl.stop;

    backyard.stop;
    backyard = (
        PfadeIn(
            Pdef(\backyard,
                    Pmono(\fftStretch_mono,
                        \dur, 0.1,
                        \amp, 1,
                        \gain, 12,
                        \buf, ~specBuff.at(\file),
                        \analysis, [~specBuff.at(\analysis)],
                        \fftSize, ~specBuff.at(\fftSize),
                        \rate, 0.2,
                        // \pos, 0.15,
                        // \pos, 0.25,
                        \pos, 0.2,
                        // \pos, 0.55,
                        // \pos, 0.1,
                        // \filter, 1,
                        \len, 0.05,
                        \out, [~mainout, ~miVerb]
                    )
                )
        , 16)
    ).play(t);
    
    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 1], inf),
            PlaceAll([4, 2, 3, 4], inf)
        )	
    );

     Pdef(\p1,
         ~makeSubdivision.(
             PlaceAll([1, 1, 1, 1], inf),
             PlaceAll([4, 2, 3], inf)
         )	
     );

     Pdef(\p3,
        ~makeSubdivision.(
            PlaceAll([1.5, 1.5, 1], inf),
            PlaceAll([3, 3, 2], inf)
        )	
    );
 
     Pdef(\p1_transform,
         ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3], reject: 1) <>
         // ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1] * 0.25, curve: \exp) <>
         // ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [2, 4], skew: [1, -1], curve: \exp) <>
         Pdef(\p1)
     );
    
    serum.stop;
    serum =
        Pbind(
            \type, \vst_midi,
            \vst, ~serumVst, // the VSTPluginController instance
            \midicmd, \noteOn, // the default, can be omitted
            \chan, 0, // MIDI channel (default: 0)
            \midinote, 43,
            \dur, Pseq([1.5, Rest(19-1.5), 1.5, Rest(20-1.5)], inf),
            \amp, 2,
            \out, [~roar, ~miVerb]
            // \gain, 6
        );
    serum = serum.play(t);
    
    cut1.stop;
    cut1 =
        // Pbind(\out, [~mainout, ~sineTracker], \pan, 0) <>
        Pbind(\out, [~roar, ~sineTracker], \pan, 0) <>
        ~filterBeat.(key: Pkey(\eventcount), beat:[2, 3], reject: 1) <>
        Pdef(\cut1) <>
        Pdef(\p3);
    cut1 = cut1.play(t);
    Pbindef(\cut1, \sliceStart, Pseq([20], inf).stutter(6));

    cut2.stop;
    cut2 = 
        Pbind(\out, [~roar, ~sineTracker]) <>
        Pdef(\cut2) <>
        Pdef(\p2);
    cut2 = cut2.play(t);

    cut3.stop;
    cut3 = 
        Pbind(\out, [~roar, ~miVerb]) <>
        Pdef(\cut3) <>
        // ~filterBeat.(key: Pkey(\eventcount), beat:[3]) <>
        ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3], reject: 1) <>
        Pdef(\p2);
    cut3 = cut3.play(t);

    kick.stop;
    kick =
        Pbind(\out, [~roar, ~convolve_A]) <>
        Pdef(\kick)  <>

    //   ~filterBeat.(key: Pkey(\eventcount), beat:[2, 3], reject: 1) <>
        Pdef(\p3);
    kick = kick.play(t);
    
    synth =
        Pbind(\out, [~roar]) <>
        // Pbind(\ctranspose, 5) <>
        Pdef(\synth);
    synth = synth.play(t);

    sectionLength.wait;
    1.wait;

    ///////////////////////////////////////////////

    sectionLength = 36;
    cut1.stop;
    cut2.stop;
    cut3.stop;
    kick.stop;
    serum.stop;
    bowl.stop;

    sectionLength.wait;

    /////////////////////////////////////////////////

    \f.postln;
    sectionLength = 64;

    // morph.stop;

    roar.stop;
    bowl.play(t);
    roar = (
        Pbindf(
            Pdef(\roar),
            \dur, 0.01,
            \drive, 9.0,
            \tone, ~pmodenv.(Pseq([-0.99, 0.75], inf), 0.5),
            \toneFreq, 500.0,
            \toneComp, 1.0,
            \drywet, 0.8,
            \bias, 0,
            // \filterFreq, 6000,
            \filterLoHi, ~pmodenv.(Pseq([0, 0.75], inf), 1),
            \filterBP, 0,
            \filterRes, 0.3,
            \filterBW, 0.5,
            \filterPre, 1.0,
            \feedAmt, 7.0,
            \feedFreq, 50.0,
            \feedBW, 0.1,
            \feedDelay, 0.1,
            \feedGate, 0.05,
            \gain, -9.0,
            \amp, 1.0,
            \group, ~morphGroup,
            \out, [~mainout, ~sineTracker]
        )
    ).play(t);
    
    synth.stop;

    Pbind(\tempo, 150/60, \dur, 0.1, \instrument, \rest).play(t);
    
    cut3.stop;
    cut3 = (
        Pbind(\out, [~roar , ~convolve_B]) <>
        PfadeOut(Pdef(\cut3), 64) <>
        Pdef(\p1_transform)
    ).play(t);
    
    backyard.stop;
    backyard =
        PfadeIn(
            Pdef(\backyard,
                    Pmono(\fftStretch_magAbove_mono,
                        \dur, 0.1,
                        \amp, 1,
                        \gain, 25,
                        \buf, ~specBuff.at(\file),
                        \analysis, [~specBuff.at(\analysis)],
                        \fftSize, ~specBuff.at(\fftSize),
                        \rate, 0.01,

                        \pos, 0.15,
                        // \pos, 0.25,
                        // \pos, 0.01,
                        // \pos, 0.55,
                        // \pos, 0.7,

                        \filter, 1,
                        // \pos, Pstep(Pseq([0.7, 0.55], inf), 4),
                        \len, 0.2,
                        \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, 1, \sin),
                        \out, [~filter, ~miVerb]
                    )
                )
        , 8
    ).play(t);

    24.wait;

    Pdef(\p3,
        ~makeSubdivision.(
            PlaceAll([1.5, 1.5, 1], inf),
            PlaceAll([3, 3, 2], inf)
        )	
    );

    cut1 =
        (
        Pbind(\out, [~roar], \dec, Pkey(\dur) * 2, \pan, 0) <>
        // PfadeOut(Pdef(\kick), 128) <>
        Pdef(\cut1) <>
        // ~filterBeat.(key: Pkey(\eventcount), beat:[1, 4], reject: 1) <>
        ~filterBeat.(key: Pkey(\eventcount), beat:[1]) <>
        // ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 2, 3], skew: [-1, 1, -1] * 0.25, curve: \exp) <>
        Pdef(\p3)
    ).play(t);


    64.wait;
    cut1.stop;
    backyard.stop;
    cut3.stop;
    bowl.stop;

}.fork(t);
)