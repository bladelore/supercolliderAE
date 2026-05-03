(
    ~break = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/CityCountryMeworkumdrache.wav", ~break, 0.3, \centroid, chans: 2);
    ~roar_A= nil ?? {Bus.audio(s,2)};
    ~roar_B = nil ?? {Bus.audio(s,2)};
)

k.gui

(
    var player;

    var delayArr = [16, 8, 6, 4, 2];

    t.tempo = 132/60;

    player = Conductor(\player, t);
    player.quant_(0);
    player.targetSection_(nil);
    // player.targetSection_(\marker);
    player.listen((type: \modality, device: k, key: \tr, button: \fwd));

    x = {
            
        Ndef(\roar_A).clear;
        
        Ndef(\roar_A, \roar)
            .set(
                \inbus, ~roar_A,
                \drive, 6.0,
                \toneFreq, 500.0,
                \toneComp, 0,
                \drywet, 1,
                \bias, 0,
                \filterFreq, 50,
                \filterBP, 0,
                \filterRes, 0.3,
                \filterBW, 0.5,
                \filterPre, 1.0,
                \feedAmt, 9.0,
                \feedFreq, 50.0,
                \feedBW, 0.1,
                \feedDelay, 0.1,
                \feedGate, 0.05,
                \gain, 6.0,
                \amp, 1.0,
            );

        Ndef(\lfo1, { SinOsc.ar(t.tempo / \rate.kr(2), pi).linlin(-1, 1, -0.99, 0.75) });
        Ndef(\lfo2, { SinOsc.ar(t.tempo / \rate.kr(1), pi).linlin(-1, 1, 0, 1) });

        Ndef(\lfo1)[999] = \pset -> Pbind(\rate, ~knob.(0).linexp(0, 1, 1, 32).ceil.reciprocal * 8, \dur, 0.01);
        Ndef(\lfo2)[999] = \pset -> Pbind(\rate, ~knob.(1).linexp(0, 1, 1, 32).ceil.reciprocal * 8, \dur, 0.01);

        Ndef(\roar_A).map(
            \tone, Ndef(\lfo1),
            \filterLoHi, Ndef(\lfo2)
        );

        Ndef(\roar_B).clear;
        Ndef(\roar_B).fadeTime = 10;
        Ndef(\roar_B, \roar)
            .set(
                \inbus, ~roar_B,
                \drive, 6.0,
                \toneFreq, 500.0,
                \toneComp, 1,
                \drywet, 1,
                \bias, 0,
                \filterFreq, 50,
                \filterBP, 0,
                \filterRes, 0.3,
                \filterBW, 0.5,
                \filterPre, 1.0,
                \feedAmt, 9.0,
                \feedFreq, 50.0,
                \feedBW, 0.1,
                \feedDelay, 0.1,
                \feedGate, 0.05,
                \gain, 18.0,
                \amp, 1.0,
            );

        Ndef(\roar_B).map(
            \tone, Ndef(\lfo1),
            \filterLoHi, Ndef(\lfo2)
        );

        Ndef(\roar_A).play([~bus1.channels, ~delay.channels]);
        Ndef(\roar_B).play([~bus2.channels, ~delay.channels]);

        Ndef(\verb, \miVerb)
        .set(
            \amp, 1,
            \time, 0.99,
            \timeMod, 0,
            \hp, 0.1,
            \damp, 0.1,
            \dampMod, 1,
            \inbus, ~miVerb
        ).play(~bus4);

        Ndef(\delay, \echo)
        .set(
            \amp, 1,
            \fb, 0.5,
            \modSpeed, 4,
            \inbus, ~delay
        ).play(~bus2);

        Ndef(\delay)[999] = \pset -> Pbind(
            \lpf, ~slider.(5).linexp(0,1,500,10000),
            \size, t.tempo / ~slider.(6).linlin(0, 1, 1, 16),
            \amp, ~slider.(7), \dur, 0.01
        );


        //////////////////////////////////////////////////////////////////////////////////
        player.label;

            Pdef(\p1,
                ~makeSubdivision.(
                    PlaceAll([1, 2, 2], inf),
                    PlaceAll([4, 2, 2, 4], inf)
                )
            );

            Pdef(\p2,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 1], inf),
                    PlaceAll([4, 4, 4, 4], inf)
                )
            );

            Pdef(\fmStringParams,
                PmonoArtic(
                    \fmStringMono,
                    \midiPitch, Pseq([42, 53].midicps, inf),
                    \legato, Pwrand([0.5, 1], [0.75, 0.25], inf),
                    \globalLag, 16,
                    \atk, ~knob.(2),
                    \rel, 0,
                    // \filter, ~knob.(3).linexp(0,1,50,5000),
                    \filter, Pexprand(50, 2000, inf),
                    \fb, ~knob.(4).linlin(0,1,-1,1),
                    \fuzz, ~knob.(5).linexp(0,1,0.01, 1) - 0.01,
                    \subharmonic, 2,
                    // \atk, 2,
                    \dec, 0.1,
                    \sus, 1,
                    \rel, 0.2,
                    // \exciterFilter, 300,
                    \exciterAttack, 3000,
                    \exciterRelease, Pseq([1000, 3000, 3000], inf),
                    \gain, -12,
                    \out, [~roar_B, ~miVerb], 
                )     
            );

            Pdef(\fmString,
                Pdef(\fmStringParams)
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2], mod: 3, reject: 1)
                <> ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 3, reject: 0)
                <> Pdef(\p2)
            ).play(t);

        player.wait;

        player.label;

            Pdef(\fmStringParams,
                PmonoArtic(
                    \fmStringMono,
                    \midiPitch, Pseq([42, 53, 62].midicps, inf),
                    \legato, Pwrand([0.5, 1], [0.75, 0.25], inf),
                    \globalLag, 16,
                    \atk, ~knob.(2),
                    \rel, 0,
                    \filter, ~knob.(3).linexp(0,1,50,5000),
                    \fb, ~knob.(4).linlin(0,1,-1,1),
                    \fuzz, ~knob.(5).linexp(0,1,0.01, 1) - 0.01,
                    \subharmonic, 2,
                    // \atk, 2,
                    \dec, 0.1,
                    \sus, 1,
                    \rel, 0.2,
                    // \exciterFilter, 300,
                    \exciterAttack, 3000,
                    \exciterRelease, Pseq([1000, 3000, 3000], inf),
                    \gain, -12,
                    \out, [~roar_B, ~miVerb], 
                )     
            );

            Pdef(\fmString,
                Pdef(\fmStringParams)
                // <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2], mod: 3, reject: 1)
                <> ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 3, reject: 0)
                <> Pdef(\p2)
            ).play(t);

        player.wait;

        player.label;

            Pdef(\kick, Pbind(\instrument, \fmKick2, \dur, 8, \freq, 60)).play(t);

            Pdef(\fmString_hat,
                Pbind(
                    \dur, 1/2,
                    \instrument, \fmString,
                    \midiPitch, 60.midicps(),
                    // \midiPitch, 79.midicps(),
                    // \midiPitch, Pseq([78, 79, 78, 81, 42].midicps, inf),
                    \pitchLag, 3,
                    \atk, 0.05,
                    \rel, 0.5,
                    \fb, 1,
                    \filter, 800,
                    \fuzz, 1,
                    \subharmonic, 1.1,
                    \exciterAttack, 0,
                    \exciterRelease, 100,
                    // \exciterRelease, Pseq([1000, 3000, 3000] * 10, inf),
                    \gain, -20,
                    \out, [~roar_A],
                ) 
            ).play(t);

        player.wait;

        player.label;

            Pdef(\fmStringParams,
                PmonoArtic(
                    \fmStringMono,
                    \midiPitch, Pseq([42, 53, 62, 64].midicps, inf),
                    \legato, Pwrand([0.5, 1], [0.75, 0.25], inf),
                    \globalLag, 16,
                    \atk, ~knob.(2),
                    \rel, 0,
                    \filter, ~knob.(3).linexp(0,1,50,5000),
                    \fb, ~knob.(4).linlin(0,1,-1,1),
                    \fuzz, ~knob.(5).linexp(0,1,0.01, 1) - 0.01,
                    \subharmonic, 2,
                    // \atk, 2,
                    \dec, 0.1,
                    \sus, 1,
                    \rel, 0.2,
                    // \exciterFilter, 300,
                    \exciterAttack, 3000,
                    \exciterRelease, Pseq([1000, 3000, 3000], inf),
                    \gain, -12,
                    \out, [~roar_B, ~miVerb], 
                )     
            );

            Pdef(\fmString,
                Pdef(\fmStringParams)
                // <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2], mod: 3, reject: 1)
                <> ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 3, reject: 0)
                <> Pdef(\p2)
            ).play(t);

        player.wait;

        player.label;

            Pdef(\p1,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 2], inf),
                    PlaceAll([2, 4, 4, 4], inf)
                )
            );

            // Pdef(\fmStringParams,
            //     PmonoArtic(
            //         \fmStringMono,
            //         \midiPitch, Pseq([42, 53, 41].midicps, inf).stutter(3),
            //         \legato, Pwrand([0.5, 1], [0.75, 0.25], inf),
            //         \globalLag, 16,
            //         \atk, ~knob.(2),
            //         \rel, 0,
            //         // \filter, ~knob.(3).linexp(0,1,50,5000),
            //         \filter, Pexprand(500, 5000, inf),
            //         \fb, ~knob.(4).linlin(0,1,-1,1),
            //         \fuzz, ~knob.(5).linexp(0,1,0.01, 1) - 0.01,
            //         \subharmonic, 2,
            //         // \atk, 2,
            //         \dec, 0.1,
            //         \sus, 1,
            //         \rel, 0.2,
            //         // \exciterFilter, 300,
            //         \exciterAttack, 3000,
            //         \exciterRelease, Pseq([1000, 3000, 3000], inf),
            //         \gain, -12,
            //         \out, [~roar_B, ~miVerb], 
            //     )     
            // );

            Pdef(\fmString,
                Pdef(\fmStringParams)
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2], mod: 3, reject: 1)
                // <> ~filterBeat.(key: Pkey(\groupcount), beat:[1, 3], mod: 3, reject: 0)
                <> Pdef(\p2)
            ).play(t);


            // Pdef(\fmString_high,
            //     Pbind(
            //         \instrument, \fmString,
            //         // \midiPitch, 54.midicps(),
            //         // \midiPitch, 79.midicps(),
            //         \midiPitch, Pseq([78, 79, 78, 81, Rest(42)].midicps, inf),
            //         \pitchLag, 3,
            //         \atk, 0,
            //         \rel, 1,
            //         \fb, -1,
            //         \filter, 5000,
            //         \fuzz, 0,
            //         \subharmonic, 3,
            //         \exciterAttack, 3000,
            //         \exciterRelease, 1000,
            //         \exciterRelease, Pseq([1000, 3000, 3000, 6000] * 2, inf),
            //         \gain, -30,
            //         \pan, Pwhite(-1, 1, inf),
            //         \out, [~roar_A, ~miVerb], 
            //     ) 
            //     <> ~filterBeat.(key: Pkey(\cyclecount), beat:[2], mod: 3, reject: 1)
            //     <> Pdef(\p1)
            // ).play(t);



        player.wait;
        
    }.fork(t);
)

(
    Pdef(\fmString_high2).stop;
    Ndef(\sample).stop(fadeTime: 10);
    Ndef(\miVerb).stop(fadeTime: 10);
)

Prout

Task