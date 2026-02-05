
(
SynthDef(\fmPerc2, {|gate=1|
    var sig, freq, pitchEnv, atk, dec, sweep, fb, ratio, drive, index, detune, noise, fbIn, car, mod;
    // var gate = 1;
    freq = \freq.kr(440);
    atk = \atk.kr(0.04);
    dec = \dec.kr(0.5);
    fb = \fb.kr(0);
    index = \index.kr(4);
    ratio = \ratio.kr(2);
    drive = \drive.kr(0);
    sweep = \sweep.kr(8);
    detune = 2**(\spread.kr(20) / 1200);
    noise = \noise.kr(1);

    fbIn = LocalIn.ar(2) * \feedback.kr(1);
    fbIn = Rotate2.ar(fbIn[0], fbIn[1], LFNoise2.ar(0.25) * \fbmod.kr(0));

    pitchEnv = (1 + (sweep * Env.perc(0.0, 0.1, curve: -4).ar)) * XLine.ar(1, 0.2 , sweep.reciprocal);
    mod = PulseDPW.ar([freq, freq * detune] * pitchEnv, \pulseWidth.kr(0.25)) * index;
    // mod = Pulse.ar([freq, freq * detune] * pitchEnv) * index;
    car = SinOscFB.ar([freq, freq * detune] * (ratio * mod + fbIn), fb) * EnvGen.kr(Env.perc(atk, dec, -4));
    sig = car + (BrownNoise.ar() * XLine.ar(1, 0.1 , dec) * noise * EnvGen.kr(Env.perc(atk, dec, 1, 1), gate));

    sig = BLowShelf.ar(sig, \lofreq.kr(500), 1, \lodb.kr(10));
    sig = BPeakEQ.ar(sig, \midfreq.kr(1200), 1, \middb.kr(-10));
    sig = BHiShelf.ar(sig, \hifreq.kr(7000), 1, \hidb.kr(0));

    // sig = (sig * drive.neg.dbamp).distort * drive.dbamp;
    sig = SelectX.ar(0.5, [sig, (Ringz.ar(sig, freq*0.25, 0.4) * -30.dbamp)]);

    // sig + (GVerb.ar(sig, \roomsize.kr(3), \reverbtime.kr(5), spread: 16) * -15.dbamp);

    sig = Compander.ar(sig, sig,
        thresh: 0.5,
        slopeAbove: 0.5,
        clampTime: 0.01,
        relaxTime: 1,
    );

    sig = sig * \gain.kr(-20).dbamp;
    sig = Pan2.ar(sig, \pan.kr(0));
    sig = LeakDC.ar(in: sig, coef: 0.995);
    sig = sig * \amp.kr(1);
    LocalOut.ar(sig+car+mod);
    // DetectSilence.ar(sig, doneAction: 2);
    Out.ar(\out.kr(0), sig);
}).add;
)

(
t = TempoClock.new(180/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

// Pdef(\p1,
//     ~makeSubdivision.(
//         PlaceAll(
//             [1.5, 1, 1.5]
//             , inf),
//         PlaceAll([[4, 1], 4, 1, 2], inf)
//     )
// );

// Pdef(\p1,
//     ~makeSubdivision.(
//         PlaceAll([1.5, 1.5, 1, 1, 0.5], inf),
//         PlaceAll([4, 4, 4], inf)
//     )
// );

Pdef(\p1,
    ~makeSubdivision.(
        PlaceAll([1, 1, 0] * 2, inf),
        PlaceAll([4, [4, 0], 4], inf)
    )
);

Pdef(\drum, 
	Pbind(
		\freq, Pseq([30, 440], inf).stutter(3),
		\atk, 0.04,
		// \dec, Pkey(\groupdelta).lincurve(0, 1, 0.1, 0.5),
        \dec, 0.01,
        // \rel, 0.01,
		// \fb, 1,
        \pulseWidth, Pkey(\groupdelta) + 0.25,
		\index, (Pkey(\groupdelta).lincurve(1, 0, 3, 4) * 0.4).stutter(3),
		\ratio, 0.5,
		\sweep, 1.0,
		\spread, 20.0,
		\noise, 0,
        \drive, 0,
        // \feedback, -2,
		\feedback, ((1-Pkey(\groupdelta)).linlin(0,1,3,4)).stutter(3),
		// \fbmod, 1,
		\lofreq, 500.0,
		\lodb, 15.0,
		\midfreq, 1200.0,
		\middb, -12.0,
		\hifreq, 7000.0,
		\hidb, 10.0,
		\gain, -20.0,
		\pan, 0.0,
        // \amp, 1,
		\amp, Pkey(\groupdelta).lincurve(0, 1, 1, 0.1),
	)
    <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 3, 5], mod: 7)
    <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2], skew: [0.5, -0.5], curve: \exp)
    // <> Pbind(\instrument, Pfunc({ |event|
	// 	var delta = event[\groupdelta];
    //     if(event[\instrument] != \rest){
    //         x = case { delta >= 0.66 } { \rim1 }
    //         // { delta >= 0.33 } { \rim1 }
    //         { delta >= 0 } { \fmPerc2 };
    //         x;
    //     } { 
    //         event[\instrument];
    //     }
    // }))
    <> Pdef(\p1)
    <> Pbind(\instrument, \fmPerc2)
).play(t)
)

(
    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll(
                [1.5, 1, 1.5]
                , inf),
            PlaceAll([[4, 1], 4, 1, 2], inf)
        )
    );
    
    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1.5, 1.5, 1], inf),
            PlaceAll([4, 4, 4], inf)
        )
    );
    
    // Pdef(\p1,
    //     ~makeSubdivision.(
    //         PlaceAll([1, 1, 1], inf),
    //         PlaceAll([4, 4, 4], inf)
    //     )
    // );
    
    Pdef('240927_165706', 
        Pbind(
            // \freq, Pseq([30, 440], inf).stutter(4),
            \freq, 440,
            \atk, 0.04,
            \dec, Pkey(\groupdelta).lincurve(0, 1, 0.1, 0.5),
            // \dec, 0.5,
            // \fb, 1,
            \pulseWidth, Pkey(\groupdelta) + 0.25,
            \index, (Pkey(\groupdelta).lincurve(1, 0, 4, 1) * 0.4).stutter(3),
            \ratio, 0.5,
            \sweep, 9.0,
            \spread, 20.0,
            \noise, 0,
            \drive, 20,
            // \feedback, -2,
            \feedback, ((1-Pkey(\groupdelta)).linlin(0,1,4,1)).stutter(3),
            \fbmod, 0,
            \lofreq, 500.0,
            \lodb, 15.0,
            \midfreq, 1200.0,
            \middb, -12.0,
            \hifreq, 7000.0,
            \hidb, 10.0,
            \gain, -20.0,
            \pan, 0.0,
            \amp, 1,
            \amp, Pkey(\groupdelta).lincurve(0, 1, 1, 0.5),
        )
        // <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 3, 5], mod: 7)
        <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2], skew: [0.5, -0.5], curve: \exp)
        <> Pdef(\p1)
        <> Pbind(\instrument, \membraneLo)
    ).play(t)
)

(
    b = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./NEW sample lib/1030 Rave Stabs/100 percent.wav");
    
    c = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./EchoThiefImpulseResponseLibrary/Brutalism/GeiselLibrary.wav");
        
    d = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./EchoThiefImpulseResponseLibrary/Miscellaneous/MillsArtMuseum.wav");
)

(
t = TempoClock.new(180/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

Pdef(\p2,
    ~makeSubdivision.(
        PlaceAll([1, [1, 0.5], 1, 4], inf),
        PlaceAll([4, 4, 1], inf)
    )
);

Pdef(\p2,
    ~makeSubdivision.(
        PlaceAll([1, 1, 1, 1], inf),
        PlaceAll([4, 4, 4, 4], inf)
    )
);

Pdef(\fb2Mod,
    PmonoArtic(\fb2,
        \amp, 0.05,
        \gain, 0,
        \buf, Pseq([b, d], inf).stutter(3),
        // \buf, d,
        // \window, 4096,
        // \impulse, 5.0,
        \impulse, ~pmodenv.(Pexprand(5, 20000, inf), Pkey(\dec)),

        \window, Pseq([4096, 1024, 512], inf).stutter(3),
        \atk, 0.1,
        \dec, 0.4,
        // \dec, Pkey(\dur),
        \sustainTime, Pkey(\dur),
        // \exciter, 0.6,
        // \density, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
        \exciter, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)),
        \feedback, ~pmodenv.(Pseq([0.2, 0.4, 0.8],inf), Pseq([4, 8, 2, 4],inf)),

        \time, ~pmodenv.(Pseq([0.005, 0.001, 0.010],inf) * 1 * Pkey(\groupdelta), Pkey(\dec)),
        \damp, ~pmodenv.(Pseq([1, 0.1],inf), Pwhite(0.01, 0.4, inf)),
        // \damp, ~pmodenv.(Pkey(\groupdelta) * 1, Pkey(\dec)),

        \filter, ~pmodenv.(Pseq([500, 20000],inf), Pseq([1, 1, 2], inf)),

        \delay2, ~pmodenv.(Pseq([0, 1, 0],inf), Pseq([0.5, 0.25], inf)),

        \rev, ~pmodenv.(Pexprand(0.1, 10, inf), Pseq([2, 4], inf)),
		// \density, 1.0,
		// \bias, 10000.0,

        // \pan, ~pmodenv.(Pwhite(-0.8, 0.8, inf), Pseq([0.5, 0.25],inf)),
    )
);

    Pbindf(
        Pdef(\fb2Seq,
            Pdef(\fb2Mod)
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 4], mod: 5)
            // <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 2], skew: [-1, 1], curve: \exp)
            <> ~pSkew.(Pdef(\p2), key: Pkey(\cyclecount), group: [1, 2], skew: [-0.5, 0.5], curve: \exp)
            <> Pdef(\p2)
            // <> Pbind(\instrument, \fb2)
        ),
        \out, [~mainout]
    ).play(t);

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1, 1, 1, 4] * 2, inf),
            PlaceAll([2, [2, 0], 2], inf)
        )
    );
    
    // Pdef(\drum, 
    //     Pbind(
    //         \freq, Pseq([30, 220, 30], inf).stutter(3),
    //         \atk, 0.04,
    //         \dec, Pkey(\groupdelta).lincurve(0, 1, 0.1, 0.5),
    //         // \dec, 0.5,
    //         // \rel, 0.01,
    //         \fb, Pseq([0, 2, 0], inf).stutter(3),
    //         \pulseWidth, Pkey(\groupdelta) + 0.25,
    //         \index, (Pkey(\groupdelta).lincurve(1, 0, 3, 4) * 0.4).stutter(3),
    //         \ratio, 2,
    //         \sweep, 8.0,
    //         \spread, 20,
    //         \noise, 2,
    //         \drive, 0,
    //         // \feedback, -2,
    //         \feedback, ((1-Pkey(\groupdelta)).linlin(0,1,3,4)).stutter(3),
    //         \fbmod, 0,
    //         \lofreq, 500.0,
    //         \lodb, 15.0,
    //         \midfreq, 1200.0,
    //         // \middb, -12.0,
    //         \hifreq, 7000.0,
    //         \hidb, 10.0,
    //         \gain, -12.0,
    //         \pan, 0.0,
    //         // \amp, 1,
    //         \amp, Pkey(\groupdelta).lincurve(0, 1, 1, 0.1),
    //     )
    //     // <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 4, 5], mod: 6)
    //     <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2], skew: [0.5, -0.5], curve: \exp)
    //     <> Pdef(\p1)
    //     <> Pbind(\instrument, \fmPerc2)
    // ).play(t)
)



