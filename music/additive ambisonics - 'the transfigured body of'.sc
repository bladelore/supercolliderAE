~order = 5
~test.()

// choose an outbus, if need be
~outbus = 0


(
~encode_by_freq = { |sig, chain, dist_angle, dist_elevation, order=5|
    var radius = 1.5;
    var partialFreqs = chain[\freqs];
    var maxPartial = ArrayMax.kr(partialFreqs);
    var sigHoa;

    sigHoa = sig.collect { |channel, i|
        var scale = partialFreqs[i] / maxPartial[0];
        // var scale = 1;

        var scaleAngle = ((1 + dist_angle) * 0.5 * scale) + ((1 - dist_angle) * 0.5 * (1 - scale));
        var scaleElevation = ((1 + dist_elevation) * 0.5 * scale) + ((1 - dist_elevation) * 0.5 * (1 - scale));

        var channelTheta = scaleAngle * 2pi;
        var channelPhi = scaleElevation * pi * 0.5;

        HoaEncodeDirection.ar(channel, channelTheta, channelPhi, radius, order);
    };

    sigHoa = sigHoa.sum;
    sigHoa;
};
)


(
~encode_by_index = {|sig, chain, dist_angle, dist_elevation, order=5|
    var radius = 1.5;
    var sigHoa;
    var sigHoaRot;
    var rtt;
    
    sigHoa = sig.collect { |channel, i|
        var scale = i / chain[\numPartials];

        var scaleAngle = ((1 + dist_angle) * 0.5 * scale) + ((1 - dist_angle) * 0.5 * (1 - scale));
        var scaleElevation = ((1 + dist_elevation) * 0.5 * scale) + ((1 - dist_elevation) * 0.5 * (1 - scale));

        var channelTheta = scaleAngle * 2pi;
        var channelPhi = scaleElevation * pi * 0.5;

        
        HoaEncodeDirection.ar(channel, channelTheta, channelPhi, radius, order);
    };
    
    sigHoa = sigHoa.sum;

    sigHoa;
};
)

Ndef(\s1).fadeTime = 10;
Ndef.clear;

(

Ndef(\s1).fadeTime = 0;
Ndef(\s1).play(numChannels: (~order + 1).squared);
)

(
Ndef(\s1).fadeTime = 0;
Ndef(\s1).play(numChannels: (~order + 1).squared);
)

(
	~break = Dictionary();
    ~bow = Dictionary();
    ~guitar = Dictionary();
	~skate = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Missing Sounds 2016/04-Hobble_Break_126_PL_1.WAV", ~break, 0.3, \crest, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./contact mics/bow mic.wav", ~bow, 0.9, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./contact mics/guitar chain.wav", ~guitar, 0.9, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./contact mics/skateboard 1.wav", ~skate, 0.9, \centroid, chans: 2);

    ~piano = Dictionary();
    ~shPad1 = Dictionary();
	~shPad2 = Dictionary();
	~shPad3 = Dictionary();
	~shPad4 = Dictionary();
	~shPad5 = Dictionary();

    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Splice packs/VISIONIST_labeled_processed/VISIONIST_tonal/VISIONIST_melody/VISIONIST_melody_loops/VISIONIST_melody_loop_piano_123_Emin.wav", ~piano, 0.3, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill 4/Pulsating Ambience/MusicFX 10.wav", ~shPad1, 0.1, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill 4/Fortunate Sleep - Cat Scratchism Mix/FEEDIES 1.wav", ~shPad2, 0.1, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill 4/Subway Moan/Eerie-Edition-CD1_31.WAV", ~shPad3, 0.1, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill 2/Day of Night/SLEEPCYCL2.wav", ~shPad4, 0.1, \centroid, chans: 2);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill Origins/The Wicked End/33 Futhswatering.wav", ~shPad5, 0.1, \centroid, chans: 2);
)

(
    ~specBuff=Dictionary();
    ~specBuff2=Dictionary();
	~specBuff3=Dictionary();
    
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill 2/Day of Night/SLEEPCYCL2.wav", ~specBuff, 16384, 2);
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Ambience/Silent Hill Origins/The Wicked End/33 Futhswatering.wav", ~specBuff2, 16384, 2);
	~makeSpec.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/mother and daughter singing o magnum mysterium-sfLDOVcK7nU.wav", ~specBuff, 16384, 2)
)

(
SynthDef(\ambi_rtt, {
	var in, sig;
    var sigHoa, sigHoaRot;
    var radius = 1.5;
	in = InFeedback.ar(\inbus.kr(0), 2).sanitize;

    sig  = in.collect { |channel, i|
        HoaEncodeDirection.ar(channel, \phi.kr(0), \theta.kr(0), radius, ~order); 
    };

    sig = sig.sum;

    sig = HoaRTT.ar(
        sig,
        
        \z.kr(0),  // roll
        \x.kr(0),  // tilt
        \y.kr(0),  // tumble
        ~order
    );

	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

~ambi_rtt1 = Bus.audio(s,2);

Pdef(\ambi_rtt1,
    Pmono(\ambi_rtt,
        \amp, 1,
        \inbus, ~ambi_rtt1,
        \theta, 0,
        \phi, 0,
        \x, 0,
        \y, 0,
        \z, 0,
        \addAction, \addToTail,
        \callback, { Pdefn(\fxid, ~id) },
    )
);

~ambi_rtt2 = Bus.audio(s,2);

Pdef(\ambi_rtt2,
    Pmono(\ambi_rtt,
        \amp, 1,
        \inbus, ~ambi_rtt2,
        \theta, 0,
        \phi, 0,
        \x, 0,
        \y, 0,
        \z, 0,
        \addAction, \addToTail,
        \callback, { Pdefn(\fxid, ~id) },
    )
);
)

(
    // a = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./NEW sample lib/1030 Rave Stabs/100 percent.wav");
    a = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./NEW sample lib/1030 Rave Stabs/FALL APART.wav");
    b = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./NEW sample lib/1030 Rave Stabs/cosmic gun.wav");
    c = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./EchoThiefImpulseResponseLibrary/Brutalism/GeiselLibrary.wav");
    d = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./NEW sample lib/1030 Rave Stabs/cow prod.wav");
);

(
{
    Ndef(\s1).fadeTime = 10;
    Ndef(\s1).play(numChannels: (~order + 1).squared);
    Ndef(\s1, {
        var chain, sig;
        var rtt;
        var rate;

        chain = ~initHarmonicsChain.(harmonics: 3, sidebands: 4, freq: 400);
        chain = ~padSynthDistribution.(
            chain, 
            harmonicRatio: 1.5,
            bw: 1000,
            bwScale: 1,
            bwSkew:  1,
            stretch: 1,
            windowSkew: 0,
        );

        chain = ~generateFormants.(chain, [
            ( \freq: LFNoise1.ar(1).range(200, 600), \bandwidth: 200, \strength: 1.0 ),
            ( \freq: LFNoise1.ar(2).range(1400, 1600), \bandwidth: 100, \strength: 0.8 ),
            ( \freq: LFNoise1.ar(1.5).range(2400, 2600), \bandwidth: 120, \strength: 0.6 )
        ]);

        chain = ~addLimiter.(chain);

        sig = SinOsc.ar(
            freq: chain[\freqs],
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]),
            mul: chain[\amps]
        );
        
        sig = sig * -20.dbamp;

        sig = Compander.ar(sig, sig,
            thresh: 0.5,

            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );
        
        sig = ~encode_by_freq.(sig, chain, SinOsc.ar(0.4), -0.5);

        sig.sanitize;
    });

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 2], inf),
            PlaceAll([3, 4, 5, 4], inf)
        )
    );

    Pbindf(
        Pdef(\ambi_rtt1),
        \gain, -9,
        \theta, 0,
        \phi, -1,
        // \y, 2pi,
        \z, ~pmodenv.(Pwhite(0, 2pi, inf), 2),
        \x, ~pmodenv.(Pwhite(0, 2pi, inf), 2),
        \y, ~pmodenv.(Pwhite(0, pi*0.5, inf), 2),
    ).play(t);
        
    16.wait;

    Pdef(\convolvedImpulse).stop;

    16.wait;

    //B

    Pdef(\impulseMod,
        PmonoArtic(\fb2,
            \amp, 0.05,
            \gain, 18,
            \buf, a,
            \impulse, 5.0,
            \window, 4096,
            // \impulse, ~pmodenv.(Pexprand(5, 20000, inf), Pkey(\dec)),
            // \window, Pseq([4096, 1024, 512], inf).stutter(3),
            \atk, 0.1,
            \dec, 0.4,
            \sustainTime, Pkey(\dur),
            \exciter, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pseq([4, 8, 2, 4],inf)),
            \time, ~pmodenv.(Pseq([0.005, 0.001, 0.010],inf) * 1 * Pkey(\groupdelta), Pkey(\dec)),
            \damp, ~pmodenv.(Pseq([1, 0.1],inf), Pwhite(0.01, 0.4, inf)),    
            \filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),
            \delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5, 0.25], inf)),    
            \rev, ~pmodenv.(Pexprand(0.1, 10, inf), Pseq([2, 4], inf)),
        )
    );
    
    Pdef(\convolvedImpulse,
            Pbind (\out, [~ambi_rtt1])
            <> Pdef(\impulseMod)
            <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 2], skew: [-1, 1], curve: \linear)
            // <> ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [2, 3], skew: [-0.5, 0.5], curve: \exp)
            <> Pdef(\p2)
    ).play(t);

    16.wait;

    Pdef(\convolvedImpulse).stop;

    16.wait;

    Ndef(\s1, {
        var chain, sig;
        var rtt;
        var rate;

        chain = ~initHarmonicsChain.(harmonics: 3, sidebands: 4, freq: 400);
        chain = ~padSynthDistribution.(
            chain, 
            harmonicRatio: 1.25,
            bw: 1000,
            bwScale: 1,
            bwSkew:  0,
            stretch: 1,
            windowSkew: 0,
        );

        chain = ~generateFormants.(chain, [
            ( \freq: LFNoise1.ar(1).range(200, 600), \bandwidth: 200, \strength: 1.0 ),
            ( \freq: LFNoise1.ar(2).range(1400, 1600), \bandwidth: 100, \strength: 0.8 ),
            ( \freq: LFNoise1.ar(1.5).range(2400, 2600), \bandwidth: 120, \strength: 0.6 )
        ]);

        chain = ~addLimiter.(chain);

        sig = SinOsc.ar(
            freq: chain[\freqs],
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]),
            mul: chain[\amps]
        );
        
        sig = sig * -20.dbamp;

        sig = Compander.ar(sig, sig,
            thresh: 0.5,

            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );

        sig = ~encode_by_freq.(sig, chain, SinOsc.ar(1), -0.5);
        
        rate = 0.5;
        rtt = SinOsc.kr(LFNoise2.kr(1.dup(3)).range(0.75 * rate, 1.25 * rate)).linlin(-1, 1, 0, 0.5 * pi);
        sig = HoaRTT.ar(
            sig,
            rtt[0],  // roll
            rtt[1],  // tilt
            rtt[2],  // tumble
            ~order
        );

        sig.sanitize;
    });

    Pdef(\impulseMod,
        PmonoArtic(\fb2,
            \amp, 0.05,
            \gain, 12,
            \buf, d,
            \impulse, 5.0,
            \window, 4096,
            // \impulse, ~pmodenv.(Pexprand(5, 20000, inf), Pkey(\dec)),
            \window, Pseq([4096, 1024, 512] * 0.5, inf).stutter(3),
            \atk, 0.1,
            \dec, 0.4,
            \sustainTime, Pkey(\dur),
            \exciter, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pseq([4, 8, 2, 4],inf)),
            \time, ~pmodenv.(Pseq([0.005, 0.001, 0.010],inf) * 1 * Pkey(\groupdelta), Pkey(\dec)),
            \damp, ~pmodenv.(Pseq([1, 0.1],inf), Pwhite(0.01, 0.4, inf)),    
            \filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),
            \delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5, 0.25], inf)),    
            \rev, ~pmodenv.(Pexprand(0.1, 10, inf), Pseq([2, 4], inf)),
        )
    );
    
    Pdef(\convolvedImpulse,
            Pbind (\out, [~ambi_rtt1])
            <> Pdef(\impulseMod)
            <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 2], skew: [-1, 1], curve: \linear)
            <> Pdef(\p2)
    ).play(t);

    32.wait;

    Pdef(\convolvedImpulse).stop;

    16.wait;

    Pdef(\p2,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 2], inf),
            PlaceAll([4, 4, 4, 4], inf)
        )
    );

    Pdef(\impulseMod,
        PmonoArtic(\fb2,
            \amp, 0.05,
            \gain, -6,
            \buf, c,
            \impulse, 5.0,
            \window, 4096,
            \impulse, ~pmodenv.(Pexprand(5, 20000, inf), Pkey(\dec)),
            // \window, Pseq([4096, 1024, 512] * 2, inf),
            \atk, 0.1,
            \dec, 0.4,
            \sustainTime, Pkey(\dur),
            \exciter, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
            \feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pseq([4, 8, 2, 4],inf)),
            \time, ~pmodenv.(Pseq([0.005, 0.001, 0.010],inf) * 1 * Pkey(\groupdelta), Pkey(\dec)),
            \damp, ~pmodenv.(Pseq([1, 0.1],inf), Pwhite(0.01, 0.4, inf)),    
            \filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),
            \delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5, 0.25], inf)),    
            \rev, ~pmodenv.(Pexprand(0.1, 10, inf), Pseq([2, 4], inf)),
        )
    );
    
    Pdef(\convolvedImpulse,
            Pbind (\out, [~ambi_rtt1])
            <> Pdef(\impulseMod)
            <> ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2], skew: [-1, 1], curve: \linear)
            <> Pdef(\p2)
    ).play(t);

    16.wait;

    Pdef(\convolvedImpulse).stop;

    8.wait;
    
    Pdef(\shPad,
        Pmono(
            \warpSlicer,
            \amp, 1,
            \gain, -12,
            \dur, 0.01,
            \buf, ~shPad2.at(\file),
            \posRate, ~pmodenv.(Pwhite(0.5, 2, inf), 0.5),
            // \posRate, ~knob.(6).linlin(0,1,0.2,2),
            \posRate, 0.01,
            \oneshot, 0,
            \sliceStart, 0,
            \pitch, -4.midiratio,
            \windowSize, ~pmodenv.(Pwhite(0.2, 1, inf), Pkey(\dur)),
            // \windowSize, 0.5,
            // \overlaps, 8, 
            \windowRandRatio, 0.2,
            \rel, 5,
            \out, ~ambi_rtt2,
            \slice, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~shPad2),
        )
    ).play(t);

    Pbindf(
        Pdef(\ambi_rtt2),
        \gain, -9,
        \phi, 2,
        // \z, ~pmodenv.(Pwhite(0, 2pi, inf), 2),
        // \x, ~pmodenv.(Pwhite(0, 2pi, inf), 2),
    ).play(t);

    64.wait;

    Ndef(\s1, {
        var chain, sig;
        var rtt;
        var rate;

        chain = ~initHarmonicsChain.(harmonics: 3, sidebands: 4, freq: 400);
        chain = ~padSynthDistribution.(
            chain, 
            harmonicRatio: 1,
            bw: 1000,
            bwScale: 1,
            bwSkew:  0,
            stretch: 1,
            windowSkew: 0,
        );
        chain = ~addLimiter.(chain);

        sig = SinOsc.ar(
            freq: chain[\freqs],
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]),
            mul: chain[\amps]
        );
        
        sig = sig * -20.dbamp;

        sig = Compander.ar(sig, sig,
            thresh: 0.5,

            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );

        sig = ~encode_by_freq.(sig, chain, SinOsc.ar(1), -0.5, order: 5);

        sig.sanitize;
    });

    Pdef(\slicer,
        Pbind(
            \instrument, \specSlicer,
            \amp, 1,
            \speed, Pwrand([0.75, 0.5, 0.25], [0.5, 0.25, 0.25], inf),
            \chance, 0.5,
            \atk, ~pmodenv.(Pwhite(0, 4, inf), Pkey(\dur)),
            \rel, 4,

            \dur, Pfunc({|ev|
                var val;
                var chance = ev[\chance].coin;
                var speed = ev[\speed];
                if(chance == true) {val = speed;}{ val = Rest(speed)};
                val;
            }),

            \buf, ~guitar.at(\file),
            \rate, Pwrand([1, 0.5], [0.75, 0.25], inf),
            \oneshot, 1,
            // \swap, ~pmodenv.(Pwhite(0, 1, inf), Pkey(\dur)),
            \smooth, ~pmodenv.(Pwhite(0, 1, inf), Pkey(\dur)),
            \slice_A, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + 1), ~guitar),
            \slice_B, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + 250), ~guitar),
            // \out, ~multiband,
            \out, ~ambi_rtt1,
    )
    ).play(t);

    64.wait;

    Pdef(\slicer).stop;
    Ndef(\s1).stop(fadeTime:30);

    Ndef(\s2).fadeTime = 0;
    Ndef(\s2).play(numChannels: (3 + 1).squared);
    Ndef(\s2, {
        var sig, chain, file, buf, sample, src;
        var partialDrift, partialDriftFreq, partialDriftMD, phaseMD;
        var ampDrift;
        var fbIn;
        var rtt, rate;
        var bufFrames;
        file = "/Users/aelazary/Desktop/Samples etc./contact mics/bow mic.wav";
        buf = Buffer.readChannel(s, file, channels: [0]);
        bufFrames = BufFrames.kr(buf);
        
        //get fb
        fbIn = LocalIn.ar(2) * \feedback.kr(0);
        sample = PlayBuf.ar(1, buf, loop: 1, startPos: bufFrames*0.19);
        src = sample + fbIn;
        //generator funcs
        chain = ~initChain.(numPartials: 36, freq: 440);

        chain = ~extractSines.(chain, src, freqLag: \freqLag.kr(0.01), ampLag: \ampLag.kr(0.1), order: 1, transpose: -12, winSize: 1024, fftSize: 4096, hopSize: -1);	
        sig = SinOsc.ar(
            freq: chain[\freqs],
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]),
            mul: chain[\amps]
        );

        sig = Compander.ar(sig, sig,
            thresh: 0.25,
            slopeBelow: 1,
            slopeAbove: 0.1,
            clampTime:  0.01,
            relaxTime:  0.01
        );

        LocalOut.ar(sig.sanitize);

        sig = ~encode_by_freq.(sig, chain, dist_angle: SinOsc.ar(0.3), dist_elevation: -1, order: 3);
        sig * 6.dbamp;
    });

    64.wait;

    Pdef(\shPad).stop;
    Ndef(\s2).stop;

    4.wait;

    Ndef(\s2, {
        var dist_angle = LFNoise2.ar(1);
        var dist_elevation = LFNoise2.ar(0.5);
        var voices = 16;
        var detune = LFNoise2.ar(3!voices).range(-3, 3);
        var sig = SinOsc.ar(
            (60 ! voices) + detune, 
            phase: ({ Rand(0, 2pi) } ! voices),
            mul: 0.1
        );

        var radius = 1.5;
        var sigHoa;
        var rtt;
        
        sigHoa = sig.collect { |channel, i|
            var scale = i / voices;

            var scaleAngle = ((1 + dist_angle) * 0.5 * scale) + ((1 - dist_angle) * 0.5 * (1 - scale));
            var scaleElevation = ((1 + dist_elevation) * 0.5 * scale) + ((1 - dist_elevation) * 0.5 * (1 - scale));

            var channelTheta = scaleAngle * 2pi;
            var channelPhi = scaleElevation * 0.5 * pi;
            
            var polarity = i.wrap(0,1).linlin(0,1,-1,1);
            
            HoaEncodeDirection.ar(channel, channelTheta, channelPhi, radius, 5) * polarity;
        };
        
        sigHoa = sigHoa.sum;
    });
    Ndef(\s2).play(numChannels: (~order + 1).squared);

    16.wait;

    // Pdef(\slicer,
    //     Pbind(
    //         \instrument, \specSlicer,
    //         \amp, 1,
    //         // \speed, Pwrand([0.75, 0.5, 0.25], [0.5, 0.25, 0.25], inf),
    //         \speed, 0.25,
    //         \chance, 0.5,
    //         \atk, ~pmodenv.(Pwhite(0, 4, inf), Pkey(\dur)),
    //         // \atk, 0,
    //         \rel, 4,

    //         \dur, Pfunc({|ev|
    //             var val;
    //             var chance = ev[\chance].coin;
    //             var speed = ev[\speed];
    //             if(chance == true) {val = speed;}{ val = Rest(speed)};
    //             val;
    //         }),

    //         \buf, ~piano.at(\file),
    //         \rate, Pwrand([1, 2], [0.75, 0.25], inf),
    //         \oneshot, 1,
    //         \swap, ~pmodenv.(Pwhite(0, 1, inf), Pkey(\dur)),
    //         \smooth, ~pmodenv.(Pwhite(0, 1, inf), Pkey(\dur)),
    //         \slice_A, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 64) + 100), ~piano),
    //         \slice_B, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 96) + 110), ~piano),
    //         \out, ~ambi_rtt1,
    // )
    // ).play(t);

    Pdef(\shPad,
        Pmono(
            \warpSlicer,
            \amp, 1,
            \gain, -12,
            \dur, 0.01,
            \buf, ~piano.at(\file),
            \posRate, ~pmodenv.(Pwhite(0.5, 2, inf), 0.5),
            // \posRate, ~knob.(6).linlin(0,1,0.2,2),
            \posRate, 0.01,
            \oneshot, 0,
            \sliceStart, 0,
            \pitch, 0.midiratio,
            \windowSize, ~pmodenv.(Pwhite(0.2, 1, inf), Pkey(\dur)),
            // \windowSize, 0.5,
            // \overlaps, 8, 
            \windowRandRatio, 0.2,
            \rel, 5,
            \out, ~mainout,
            \slice, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~piano),
        )
    ).play(t);

    Ndef(\s2).fadeTime = 10;
    64.wait;

    Pdef(\slicer).stop;

    Ndef(\s2, {
        var dist_angle = LFNoise2.ar(1);
        var dist_elevation = LFNoise2.ar(0.5);
        var voices = 16;
        var detune = LFNoise2.ar(3!voices).range(-3, 3);

        var sig = LFSaw.ar(
            (60 ! voices) + detune, 
            iphase: ({ Rand(0, 2) } ! voices),
            mul: 0.1
        );

        var radius = 1.5;
        var sigHoa;
        var rtt;
        
        sigHoa = sig.collect { |channel, i|
            var scale = i / voices;

            var scaleAngle = ((1 + dist_angle) * 0.5 * scale) + ((1 - dist_angle) * 0.5 * (1 - scale));
            var scaleElevation = ((1 + dist_elevation) * 0.5 * scale) + ((1 - dist_elevation) * 0.5 * (1 - scale));

            var channelTheta = scaleAngle * 2pi;
            var channelPhi = scaleElevation * 0.5 * pi;
            
            var polarity = i.wrap(0,1).linlin(0,1,-1,1);
            
            HoaEncodeDirection.ar(channel, channelTheta, channelPhi, radius, 5) * polarity;
        };
        
        sigHoa = sigHoa.sum;
    }).play;

    128.wait;

    Ndef(\s2).stop;

}.fork(t);
)

Pdef(\shPad).clear

(
    Pdef(\shPad,
        Pmono(
            \warpSlicer,
            \amp, 1,
            \gain, -12,
            \dur, 0.01,
            \buf, ~shPad2.at(\file),
            \posRate, ~pmodenv.(Pwhite(0.5, 2, inf), 0.5),
            // \posRate, ~knob.(6).linlin(0,1,0.2,2),
            \posRate, 0.01,
            \oneshot, 0,
            \sliceStart, 0,
            \pitch, -4.midiratio,
            \windowSize, ~pmodenv.(Pwhite(0.2, 1, inf), Pkey(\dur)),
            // \windowSize, 0.5,
            // \overlaps, 8, 
            \windowRandRatio, 0.2,
            \rel, 5,
            \out, ~mainout,
            \slice, ~pGetSlice.((Pseries(1, 32, inf).wrap(0, 32) + Pkey(\sliceStart)), ~shPad2),
        )
    ).play(t);
)

(
{
    var dist_angle = LFNoise2.ar(1);
    var dist_elevation = LFNoise2.ar(0.5);
    var voices = 16;
    var detune = LFNoise2.ar(3!voices).range(-3, 3);
    var sig = LFSaw.ar(
        (60 ! voices) + detune, 
        iphase: ({ Rand(0, 2) } ! voices),
        mul: 0.1
    );

    // var sig = SinOsc.ar(
    //     (60 ! voices) + detune, 
    //     phase: ({ Rand(0, 2pi) } ! voices),
    //     mul: 0.1
    // );

    var radius = 1.5;
    var sigHoa;
    var rtt;
    
    sigHoa = sig.collect { |channel, i|
        var scale = i / voices;

        var scaleAngle = ((1 + dist_angle) * 0.5 * scale) + ((1 - dist_angle) * 0.5 * (1 - scale));
        var scaleElevation = ((1 + dist_elevation) * 0.5 * scale) + ((1 - dist_elevation) * 0.5 * (1 - scale));

        var channelTheta = scaleAngle * 2pi;
        var channelPhi = scaleElevation * 0.5 * pi;
        
        var polarity = i.wrap(0,1).linlin(0,1,-1,1);
        
        HoaEncodeDirection.ar(channel, channelTheta, channelPhi, radius, ~order)*polarity;
    };
    
    sigHoa = sigHoa.sum;
}.play
)