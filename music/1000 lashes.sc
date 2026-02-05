(
    b = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./NEW sample lib/1030 Rave Stabs/100 percent.wav");
    c = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./EchoThiefImpulseResponseLibrary/Brutalism/GeiselLibrary.wav");
    d = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./EchoThiefImpulseResponseLibrary/Miscellaneous/MillsArtMuseum.wav");
)

(
Pdef(\kick, 
	Pbind(
        \dur, 1,
		\instrument, \fmPerc3,
		\freq, Pseq([60.0, 90], inf),
		\atk, 0.01,
		\dec, Pkey(\dur) * 1,
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
		\feedback, Pkey(\cycledelta) * 0.25,
		\fbmod, 1,
		\pulseWidth, 1 - Pkey(\groupdelta).linlin(0,1,0.1,0.99),
		\lofreq, 500.0,
		\lodb, 10.0,
		\midfreq, 1200.0,
		\middb, 0.0,
		\hifreq, 7000.0,
		\hidb, 30.0,
		\gain, -17.0,
		\pan, 0.0,
        // \hpf, 
		\amp, Pkey(\groupdelta).linlin(0, 1, 1, 0.3),
        \out, [~mainout]

	)
);

Pdef(\snare, 
	Pbind(
		\instrument, \rim1,
		// \gate, 1.0,
		\freq, 50.0,
		\atk, 0.03,
		\dec, Pkey(\dur) * 0.5,
		\fb, 10.0,
		\index, 4.0,
		\ratio, 16.0,
		\sweep, 8.0,
		\spread, 20.0,
		\noise, 1.0,
		\roomsize, 3.0, 
		\reverbtime, 5.0,
		\gain, -20.0,
		\pan, 0.0,
		\amp, Pkey(\groupdelta).linlin(0, 1, 1, 0.3),
		// \amp, 0
	)
);

Pdef(\rim, 
	Pbind(
		\instrument, \rim1,
		// \gate, 1.0,
		\freq, 60 + Pwhite(-3, 3, inf),
		\atk, 0.03,
		\dec, 0.1,
		\fb, 0.0,
		\index, 4.0,
		\ratio, 16,
		\sweep, 128.0,
		\spread, Pkey(\groupdelta).linlin(0,1,1,20),
		\noise, 1.0,
		\roomsize, 3.0,
		\reverbtime, 5.0,
		\gain, -20.0,
		\pan, 0.0,
		\amp, Pkey(\groupdelta).linlin(0, 1, 0.8, 0.3),
		// \amp, 0
        \out, [~mainout, ~miVerb]
	)
);

Pdef(\hh, 
	Pbind(
		\instrument, \hh2,
		\freq, 1200.0,
		\fb, 1 - Pkey(\groupdelta),
		\spread, 20.0,
		\index, 0.75,
		\pulseWidth, Pkey(\groupdelta),
		\atk, 0.001,
		\dec, Pkey(\dur),
		\gain, -5.0,
		\pan, 0.0,
		// \amp, 1,
		\amp, Pkey(\groupdelta),

        // \out, [~mainout, ~miVerb]
	)
);

Pdef(\sub, 
	Pbind(
		\instrument, \simpleSub,
		\freq, Pseq([39.0], inf),
		\atk, 0.05,
		\dec, Pkey(\dur) * 2,
        // \dec, Pkey(\dur) * 0.5,
		\drive, -10.0,
		\sweep, 8.5,
		\gain, -12.0,
		\pan, 0.0,
		\amp, 1.0,
	);
);
)

(
    ~sliceBuf = Dictionary();
    ~sliceBuf2 = Dictionary();
    ~sliceBuf3 = Dictionary();
    ~sliceBuf4 = Dictionary();
    ~sliceBuf5 = Dictionary();

    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/sventojibridgecontact07261150.wav", ~sliceBuf, 0.1, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/RomssavahjohkaUnderBridge.wav", ~sliceBuf2, 0.2, \flatness);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/kusnieriunaiwatetower.wav", ~sliceBuf3, 0.2, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/cordedamarageportdajaccioLR.wav", ~sliceBuf4, 0.2, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Drum Loops/Silent Hill 2/Block Mind/loop 9 0013.wav", ~sliceBuf5, 0.2, \centroid);
)

~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/12011103amlampostwiresa33road.wav", ~sliceBuf5, 0.25, \crest);

CmdPeriodDef(\serumPresets, {
    ~serumVst = VSTPluginController(Synth(\vsti)).open("Serum");
    ~serumVst.loadPreset("springs3");
})

(
Synth(\comp,
	[
		\ratio: 6,
		\thresh, -40,
		\atk, 0.1,
		\rel, 1000,
		\makeup, 0,
		\automakeup, 0
	],
    target: g,
    addAction: \addToTail,
);
)

(
g.free;
g = Group.new(RootNode(Server.default), \addToTail);

t = TempoClock.new(140/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

Pdef(\player).stop;
Pdef(\player,
Pspawner({| sp |
    var sectionLength;
    var verb, drum, bridge, rope, sub, serum, chain, chain2, filter, follower;
    // fx const
    
    \a.postln;
    sectionLength = 64;
    // sectionLength = 0;

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1.5, Prand([1, 2], inf), 1], inf),
            PlaceAll([6, 4, 4], inf)
        )	
    );

   filter = sp.par(
        Pbindf(
            Pdef(\filter),
            \dur, 0.01,
            \blend, ~pmodenv.(Pseq([0.75, 1], inf), 4),
            \freq, 200,
            \res, 0.5,
            \out, [~mainout]
        )
    );

    // Pdef(\p1, 
    // 	~makeSubdivision.(
    // 		PlaceAll([1.5, 1.5, 1], inf),
    // 		PlaceAll([3, 3, 4], inf)
    // 	)
    // );

    // Pdef(\p1,
    // 	~makeSubdivision.(
    // 		PlaceAll([1, 1, Rest(1)], inf),
    // 		PlaceAll([2, 2, 2, 4], inf)
    // 	)
    // );

   drum = sp.par(
        Pdef(\drum,
            Pbind(\out, [~filter]) <>
            Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\snare),], Pseq([2, 2, 2, 1, 2, 2, 2, 3] - 1, inf)) 
            <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2], skew: [1], curve: \exp)
            <> ~filterBeat.(key: Pkey(\groupcount), beat:[2, 3], reject: 1)
            <> Pdef(\p1)
        )
    );

    Pbindef(\kick, \feedback, Pkey(\cycledelta) * 0.25);
    Pbindef(\kick, \dec, Pkey(\dur) * 2);

    bridge = sp.par(
        Pdef(\bridge,
            Pmono(
                \grainSlicer_mono,       
                \amp, 1,
                \buf, ~sliceBuf.at(\file),
                \sliceStart, 100,
                //lowest to highest
                \overlap, 200,
                \trigRate, 10,
                // \trigRate, Pseg(Pseq([1000, 100], inf), 8, 'exp' , inf),
                // \slice, 10,
                \slice, ~pGetSlice.(
                    (Pseries(1, 1, inf).wrap(0, 4) + Pkey(\sliceStart)).stutter(8), 
                    ~sliceBuf
                ),
                // \gain, 6,
                \posRate, 0.5,
                \rate, 1,
                // \out, 
                \gain, -9,
                \atk, 1,
                // \dec, 0.1,
                \dec, 2,
                \out, [~mainout]
            )
            <> Pdef(\p1)
        )
    );

    rope = sp.par(
        Pdef(\rope,
            Pbind(
                \instrument, \segPlayer,
                \amp, 1,
                \atk, 0.4,
                \rel, Pkey(\dur) * 4,
                \curve, Pkey(\groupdelta).linlin(0,1, -4,0),
                \buf, ~sliceBuf.at(\file),
                \rate, 1,
                \oneshot, 1,
                \gain, -6,
                \sliceStart, 200,
                \slice, ~pGetSlice.(
                    (Pseries(1, 1, inf).wrap(0, 16) + Pkey(\sliceStart)), 
                    ~sliceBuf
                ),
                \pan, ~pmodenv.(Pwhite(-1, 1, inf), Pkey(\dur)),

                // \pitchMix, 0.5,
                \pitchRatio, 2,
                \windowSize, 0.05,
                \pitchDispersion, 0.01,
                \timeDispersion, 0.05,
                \out, [~mainout, ~convolve_B],              
            )
                <> ~filterBeat.(key: Pkey(\eventcount), beat:[3])
                // <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2], skew: [1], curve: \exp)
                <> ~filterBeat.(key: Pkey(\groupcount), beat:[3])
                <> Pdef(\p1)
            )
    );

    sp.wait(sectionLength);

    /////////////////////////////////////////////
    \b.postln;
    sectionLength = 64;
    // sectionLength = 0;

    sp.suspend(chain2);
    sp.suspend(filter);
    filter = sp.par(
        Pbindf(
            Pdef(\filter),
            \dur, 0.01,
            \blend, ~pmodenv.(Pseq([0.5, 1], inf), 2),
            // \blend, ~mod1_out,
            // \freq, Pseg(Pseq([50, 9000],inf), 4, 'lin' , inf),
            \freq, 200,
            \res, 0,
            \out, [~mainout]
        )
    );
    
    serum = sp.par(
        Ppar([
            Pbind(
                \type, \vst_midi,
                \vst, ~serumVst,
                \midicmd, \noteOn, // the default, can be omitted
                \chan, 0, // MIDI channel (default: 0)
                \midinote, 15,
            )
            <> Pdef(\p1)
            ,
    
            Pbind(
                \type, \vst_set,
                \vst, ~serumVst,
                //macro 1
                218, 0.2,
                // 218, Pwhite(0.24, 0.9, inf),
                219, 0.5,
                // 219, Pwhite(0.25, 0.9, inf),
                // 220, 0,
                // 220, Pwhite(0, 1, inf),
                //atk
                35, Pseg(Pseq([0.8, 0.3], inf), 12, repeats: inf),
                \dur, 0.25
            )
        ])
    );
    
    sp.suspend(drum);
    drum = sp.par(
    Pdef(\drum,
            Pbind(\out, [~filter]) <>
            Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\sub)], Pseq([4, 2, 2, 1, 2, 2, 2, 3] - 1, inf))
            <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2], skew: [1], curve: \exp)
            <> ~filterBeat.(key: Pkey(\eventcount), beat:[2, 3], reject: 1)
            <> Pbind(\out, [~mainout, ~convolve_A])
            <> Pdef(\p1)
        )
    );
        
    sp.wait(sectionLength);
    ///////////////////////////////////////////
    \b2.postln;
    sectionLength = 64;
    // sectionLength = 0;
    sp.suspend(filter);

    sp.suspend(serum);
    serum = sp.par(
        Ppar([
            Pbind(
                \type, \vst_midi,
                \vst, ~serumVst, // the VSTPluginController instance
                \midicmd, \noteOn, // the default, can be omitted
                \chan, 0, // MIDI channel (default: 0)
                \midinote, 15,
            )
            <> Pdef(\p1)
            ,
    
            Pbind(
                \type, \vst_set,
                \vst, ~serumVst,
                //macro 1
                218, 0.2,
                // 218, Pwhite(0.24, 0.9, inf),
                219, 0.7,
                // 219, Pwhite(0.25, 0.9, inf),
                220, 1,
                // 220, Pwhite(0, 1, inf),
                //atk
                35, Pseg(Pseq([0.8, 0.3], inf), Pseq([12], inf), repeats: inf),
                \dur, 0.25
            )
        ])
    );
    
    sp.suspend(chain);
    chain = sp.par(
        PfadeIn(
            Pdef(\perc,
                Pbind(
                    \instrument, \segPlayer,
                    \amp, ~pmodenv.(Pseq([0.3, 0.8], inf), 16, ),
                    \amp, 1,
                    \atk, 0,
                    \rel, Pwhite(0.125, 0.3, inf),
                    \curve, -8,
                    \buf, ~sliceBuf5.at(\file),
                    \rate, 0.75,
                    \oneshot, 0,
                    \gain, -6,
                    \sliceStart, 0,
                    \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 32) + Pkey(\sliceStart), ~sliceBuf5),
                    \pan, ~pmodenv.(Pwhite(0.55, -0.55, inf), 0.25),
                    \pitchMix, 0.5,
                    \pitchRatio, 1,
                    \windowSize, 0.01,
                    \pitchDispersion, 0.01,
                    \timeDispersion, 0.1,
                    \out, [~convolve_B, ~filter]
                )
                <> Pbind(\dur, Pseq([0.25, 0.25], inf)).withSwing(0.25, 0.005)
        ), 16)
    );
    
    sp.suspend(drum);
    drum = sp.par(
        Pdef(\drum,
            // Pbind(\out, [~filter]) <>
            // Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\sub)], Pseq([4, 2, 2, 1, 2, 2, 2, 3] - 1, inf))
            Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\sub),], PlaceAll([4, 1, 2, 1, 2, 4, 2, 3] - 1, inf))
            // <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2], skew: [1], curve: \exp)
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[2, 3], reject: 1)
            <> Pbind(\out, [~mainout])
            <> Pdef(\p1)
        )
    );
    Pbindef(\kick, \feedback, Pkey(\cycledelta) * 1);
    Pbindef(\kick, \dec, Pkey(\dur) * 2);
    Pbindef(\sub, \dec, Pkey(\dur) * 1);

    sp.wait(sectionLength);

    ///////////////////////////////////////////////
    \c.postln;
    sectionLength = 30;
    // sectionLength = 0;

    Pdef(\p1, 
    	~makeSubdivision.(
    		PlaceAll([1.5, 1.5, 1], inf),
    		PlaceAll([3, 3, 4], inf)
    	)
    );
    
    sp.suspend(drum);
    drum = sp.par(
        Pdef(\drum,
            // Pbind(\out, [~filter]) <>
            // Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\sub)], Pseq([4, 2, 1, 4, 2, 2, 3, 2] - 1, inf))
            Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\sub)], PlaceAll([4, 1, 2, 4, 2, 2, 3, 2] - 1, inf))
            // Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\sub),], PlaceAll([4, 1, 4, 1, 2, 4, 2, 3] - 1, inf))
            // <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2], skew: [1], curve: \exp)
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[2], reject: 1)
            <> Pdef(\p1)
        )
    );

    Pbindef(\kick, \feedback, Pkey(\cycledelta) * 1);
    Pbindef(\kick, \dec, Pkey(\dur) * 2);
    Pbindef(\sub, \dec, Pkey(\dur) * 1.5);

    sp.suspend(serum);
    serum = sp.par(
        Ppar([
            Pbind(
                \type, \vst_midi,
                \vst, ~serumVst,
                \midicmd, \noteOn,
                \chan, 0,
                \midinote, 15,
            )
            <> Pdef(\p1)
            ,
            Pbind(
                \type, \vst_set,
                \vst, ~serumVst,
                //macro 1
                // 218, 0.2,
                218, Pwhite(0.24, 0.9, inf),
                // 219, 0.5,
                219, Pwhite(0.25, 0.9, inf),
                // 220, 0,
                220, Pwhite(0, 1, inf),
                //atk
                35, Pseg(Pseq([0.3, 0.8], inf), 6, repeats: inf),
                \dur, 0.25
            )
        ])
    );

    sp.wait(sectionLength);

    ///////////////////////////////////////////////
    \d.postln;
    sectionLength = 64;
    // sectionLength = 0;
    
    sp.suspend(chain2);
    
    Pdef(\p1,
    	~makeSubdivision.(
    		PlaceAll([1, 1, Rest(1)], inf),
    		PlaceAll([2, 2, 2], inf)
    	)
    );

    Pdef(\serumP2,
    	~makeSubdivision.(
    		PlaceAll([Rest(1), 1, 1], inf),
    		PlaceAll([4, 4, 4], inf)
    	)
    );
    
    sp.suspend(filter);
    filter = sp.par(
        Pbindf(
            Pdef(\filter),
            \dur, 0.01,
            \blend, ~pmodenv.(Pseq([0.5, 1], inf), 2),
            \freq, 500,
            \res, 0.5,
            \out, [~mainout]
        )
    );

    sp.suspend(drum);
    drum = sp.par(
        Pdef(\drum,
            // Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\sub)], Pseq([4, 1, 2, 4, 2, 2, 2, 3] - 1, inf))
            // Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\sub)], PlaceAll([4, 1, 2, 4, 2, 2, 3, 2] - 1, inf))
            Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\sub),], PlaceAll([4, 1, 4, 1, 2, 4, 3, 2] - 1, inf))
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[2, 3], reject: 1)
            <> Pdef(\p1)
        )
    );

    Pbindef(\kick, \feedback, Pkey(\cycledelta) * 1);
    Pbindef(\kick, \dec, Pkey(\dur) * 3);
    Pbindef(\sub, \dec, Pkey(\dur) * 3);

    sp.suspend(serum);
    serum = sp.par(
        Ppar([
            Pbind(\out, [~filter]) <>
            Pbind(
                \type, \vst_midi,
                \vst, ~serumVst,
                \midicmd, \noteOn,
                \chan, 0,
                \midinote, 15,
            )
            <> Pdef(\serumP2)
            ,
    
            Pbind(
                \type, \vst_set,
                \vst, ~serumVst,
                //macro 1
                218, 0.5,
                // 218, Pwhite(0.24, 0.9, inf),
                219, 0.5,
                // 219, Pwhite(0.0, 0.2, inf),
                // 220, 0.5,
                220, Pseg(Pseq([0, 1], inf), 2, repeats: inf),
                //atk
                35, 0.1,
                37, 0.4,
                \dur, 0.25
            )
        ])
    );

    sp.suspend(chain);
    chain = sp.par(
            Pdef(\perc,
                Pbind(
                    \instrument, \segPlayer,
                    \amp, ~pmodenv.(Pseq([0.3, 0.3, 0.8], inf), 3),
                    // \amp, 1,
                    \atk, 0,
                    \rel, Pwhite(0.125, 0.3, inf),
                    \curve, -8,
                    \buf, ~sliceBuf5.at(\file),
                    \rate, 1,
                    \oneshot, 0,
                    \gain, -6,
                    \sliceStart, 0,
                    \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 32) + Pkey(\sliceStart), ~sliceBuf5),
                    \pan, ~pmodenv.(Pwhite(0.55, -0.55, inf), 0.25),
                    \pitchMix, 0.5,
                    \pitchRatio, 1,
                    \windowSize, 0.01,
                    \pitchDispersion, 0.01,
                    \timeDispersion, 0.1,
                    \out, [~convolve_B, ~filter]
                )
                <> Pbind(\dur, Pseq([0.25, 0.25], inf)).withSwing(0.25, 0.005)
            )
    );

    sp.wait(sectionLength);
    
    ///////////////////////////////////////////////
    \e.postln;
    sectionLength = 68;
    // sectionLength = 0;

    Pdef(\p2,
    	~makeSubdivision.(
    		PlaceAll([1.5, 1.5, 1.5, 1.5, 1, 1], inf),
    		PlaceAll([2, 2, 2], inf)
    	)
    );

    sp.suspend(drum);
    Pbindef(\kick, \dec, Pkey(\dur) * 0.5).quant = 0.0;
    Pbindef(\sub, \dec, Pkey(\dur) * 1).quant = 0.0;
    
    drum = sp.par(
        Pdef(\drum,
            Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\sub)], Pseq([4, 2, 1, 1, 3, 1] - 1, inf))
            // Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\sub),], PlaceAll([4, 1, 2, 1, 2, 4, 2, 3] - 1, inf)) 
            // <> ~filterBeat.(key: Pkey(\eventcount), beat:[2, 3], reject: 1)
            <> Pdef(\p2)
        )
    );

    sp.suspend(chain);
    chain = sp.par(
        Pdef(\perc,
            Pbind(
                \instrument, \segPlayer,
                \amp, Pseq([0.8, 0.3, 0.3, 0.8, 0.3, 0.3, 0.8, 0.3, 0.3, 0.8, 0.3, 0.3, 0.8, 0.3, 0.3, 0.3], inf),
                // \amp, 1,
                \atk, 0,
                \rel, Pwhite(0.125, 0.3, inf),
                \curve, -8,
                \buf, ~sliceBuf5.at(\file),
                \rate, 1,
                \oneshot, 0,
                \gain, -6,
                \sliceStart, 0,
                \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 32) + Pkey(\sliceStart), ~sliceBuf5),
                \pan, ~pmodenv.(Pwhite(0.55, -0.55, inf), 0.25),
                \pitchMix, 0.5,
                \pitchRatio, 1,
                \windowSize, 0.01,
                \pitchDispersion, 0.01,
                \timeDispersion, 0.1,
                \out, [~convolve_B, ~filter]
            )
            <> Pbind(\dur, Pseq([0.25, 0.25], inf)).withSwing(0.25, 0.005)
        )
    );
    
    sp.suspend(bridge);
    bridge = sp.par(
        Pdef(\bridge,
            Pmono(
                \grainSlicer_mono,       
                \amp, 1,
                \buf, ~sliceBuf.at(\file),
                \sliceStart, 100,
                //lowest to highest
                \overlap, 200,
                \trigRate, 10,
                // \trigRate, Pseg(Pseq([1000, 100], inf), 8, 'exp' , inf),
                // \slice, 10,
                \slice, ~pGetSlice.(
                    (Pseries(1, 1, inf).wrap(0, 4) + Pkey(\sliceStart)).stutter(8), 
                    ~sliceBuf
                ),
                // \gain, 6,
                \posRate, 0.5,
                \rate, 1.5,
                // \out, 
                \gain, -6,
                \atk, 1,
                // \dec, 0.1,
                \dec, 2,
                \out, [~mainout, ~convolve_B]
            )
            <> Pdef(\p1)
        )
    );

    sp.suspend(rope);
    rope = sp.par(
        Pdef(\rope,
            Pbind(
                \instrument, \segPlayer,
                \amp, 1,
                \atk, 0.4,
                \rel, Pkey(\dur) * 4,
                \curve, Pkey(\groupdelta).linlin(0,1, -4,0),
                \buf, ~sliceBuf.at(\file),
                \rate, 1.5,
                \oneshot, 0,
                \gain, 0,
                \sliceStart, 200,
                \slice, ~pGetSlice.(
                    (Pseries(1, 1, inf).wrap(0, 16) + Pkey(\sliceStart)), 
                    ~sliceBuf
                ),
                \pan, ~pmodenv.(Pwhite(-0.5, 0.5, inf), Pkey(\dur)),

                \pitchMix, 0.5,
                \pitchRatio, 0.5,
                \windowSize, 0.05,
                \pitchDispersion, 0.01,
                \timeDispersion, 0.05,
                \out, [~mainout, ~convolve_B],              
            )
                // <> ~filterBeat.(key: Pkey(\eventcount), beat:[3])
                // <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2], skew: [1], curve: \exp)
                // <> ~filterBeat.(key: Pkey(\groupcount), beat:[3])
                <> Pdef(\p1)
            )
    );

    sp.suspend(serum);
    serum = sp.par(
        Ppar([
            Pbind(
                \type, \vst_midi,
                \vst, ~serumVst, // the VSTPluginController instance
                \midicmd, \noteOn, // the default, can be omitted
                \chan, 0, // MIDI channel (default: 0)
                \amp, 0.5,
                \midinote, 15,
            )
            <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 2], skew: [-0.5], curve: \exp)
            <> Pdef(\p2)
            ,
    
            Pbind(
                \type, \vst_set,
                \vst, ~serumVst,
                //macro 1
                218, 0.5,
                // 218, Pwhite(0.24, 0.9, inf),
                // 219, 0.1,
                219, Pwhite(1, 0.2, inf),
                // 220, 0,
                220, Pseg(Pseq([0, 1], inf), 2, repeats: inf),
                //atk
                35, 0.01,
                37, 0.4,
                \dur, 0.25
            )
        ])
    );

    sp.wait(sectionLength);
    
    ///////////////////////////////////////////////
    \f.postln;
    sectionLength = 64;
    // sectionLength = 0;

    Pdef(\p2,
    	~makeSubdivision.(
    		PlaceAll([1, 1, 1, 1, 1, 1], inf),
    		PlaceAll([4, 4, 4, 4], inf)
    	)
    );

    sp.suspend(drum);
    sp.suspend(serum);
    sp.suspend(chain);

    chain = sp.par(
        Pdef(\perc,
            Pbind(
                \instrument, \segPlayer,
                \amp, Pseq([0.8, 0.3, 0.3, 0.8, 0.3, 0.3, 0.8, 0.3, 0.3, 0.8, 0.3, 0.3, 0.8, 0.3, 0.3, 0.3], inf),
                // \amp, 1,
                \atk, 0,
                \rel, Pwhite(0.125, 0.3, inf),
                \curve, -8,
                \buf, ~sliceBuf5.at(\file),
                \rate, 1,
                \oneshot, 0,
                \gain, -6,
                \sliceStart, 0,
                \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 32) + Pkey(\sliceStart), ~sliceBuf5),
                \pan, ~pmodenv.(Pwhite(0.55, -0.55, inf), 0.25),
                \pitchMix, 0.5,
                \pitchRatio, 1,
                \windowSize, 0.01,
                \pitchDispersion, 0.01,
                \timeDispersion, 0.1,
                \out, [~convolve_B, ~filter]
            )
            <> Pbind(\dur, Pseq([0.25, 0.25], inf)).withSwing(0.25, 0.005)
        )
    );
    
    // Pbindef(\rim, \freq, Pseq([60, 40, 40, 30], inf)).quant = 0.0;
    Pbindef(\rim, \amp, 0.5).quant = 0.0;
    Pbindef(\kick, \dec, Pkey(\dur) * 4).quant = 0.0;
    Pbindef(\sub, \dec, Pkey(\dur) * 2).quant = 0.0;
    
    drum = sp.par(
        Pdef(\drum,
            Pbind(\out, [~filter]) <>
            Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\sub)], Pseq([4, 2, 2, 4, 2, 2, 4, 2, 3, 4, 2, 2, 4, 2, 4, 2] - 1, inf))
            // Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\sub),], PlaceAll([4, 1, 2, 1, 2, 4, 2, 3] - 1, inf)) 
            // <> ~filterBeat.(key: Pkey(\cyclecount), beat:[3], reject: 1, mod: 6)
            <> Pdef(\p2)
        )
    );

    serum = sp.par(
        Ppar([
            Pbind(
                \type, \vst_midi,
                \vst, ~serumVst,
                \midicmd, \noteOn,
                \chan, 0,
                \amp, 0.5,
                \midinote, 15,
            )
            <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 2], skew: [-0.5], curve: \exp)
            <> Pdef(\p2)
            ,
    
            Pbind(
                \type, \vst_set,
                \vst, ~serumVst,
                //macro 1
                218, 0.5,
                218, Pwhite(0.24, 0.9, inf),
                // 219, 0.1,
                219, Pwhite(1, 0.2, inf),
                // 220, 0,
                220, Pseg(Pseq([0, 1], inf), 2, repeats: inf),
                //atk
                35, 0.01,
                37, 0.4,
                \dur, 0.25
            )
        ])
    );

    // sp.suspend(bridge);
    // sp.suspend(rope);

    sp.wait(sectionLength);
    
    ///////////////////////////////////////////////
    \g.postln;
    sectionLength = 64;
    // sectionLength = 0;
    Pbindef(\sub, \dec, Pkey(\dur) * 3).quant = 0.0;
    Pbindef(\sub, \freq, Pseq([37, 34, 34, 34], inf)).quant = 0.0;
    // Pbindef(\perc, \rate, 2).quant = 0.0;
    
    sp.suspend(drum);
    drum = sp.par(
        Pdef(\drum,
            // Pbind(\out, [~filter]) <>
            Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\sub)], Pseq([4, 2, 2, 4, 2, 2, 4, 2, 3, 4, 2, 2, 4, 2, 4, 2] - 1, inf))
            // Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\sub),], PlaceAll([4, 1, 2, 1, 2, 4, 2, 3] - 1, inf)) 
            // <> ~filterBeat.(key: Pkey(\cyclecount), beat:[3], reject: 1, mod: 4)
            <> Pdef(\p2)
        )
    );

    sp.suspend(chain);
    chain = sp.par(
        Pdef(\perc,
            Pbind(
                \instrument, \segPlayer,
                \amp, Pseq([0.8, 0.3, 0.3, 0.8, 0.3, 0.3, 0.8, 0.3, 0.3, 0.8, 0.3, 0.3, 0.8, 0.3, 0.3, 0.3], inf),
                // \amp, 1,
                \atk, 0,
                \rel, Pwhite(0.125, 0.3, inf),
                \curve, -8,
                \buf, ~sliceBuf5.at(\file),
                \rate, 2,
                \oneshot, 0,
                \gain, -6,
                \sliceStart, 0,
                \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 32) + Pkey(\sliceStart), ~sliceBuf5),
                \pan, ~pmodenv.(Pwhite(0.55, -0.55, inf), 0.25),
                \pitchMix, 0.5,
                \pitchRatio, 1,
                \windowSize, 0.01,
                \pitchDispersion, 0.01,
                \timeDispersion, 0.1,
                \out, [~convolve_B, ~filter]
            )
            <> Pbind(\dur, Pseq([0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.125, 0.125], inf)).withSwing(0.25, 0.005)
        )
    );

    chain2 = sp.par(Pdef(\pitched,
        Pbind(
            \instrument, \grainSlicer,       
            \amp, 1,
            \buf, ~sliceBuf5.at(\file),
            \sliceStart, 6,
            //lowest to highest
            \overlap, 200,
            // \trigRate, 400,
            \trigRate, Pseg(Pseq([1000, 20], inf), 3, 'sine' , inf),
            // \slice, 10,
            // \oneshot, 1,
            \slice, ~pGetSlice.((Pseries(1, 1, inf) + Pkey(\sliceStart)), ~sliceBuf5),
            // \gain, 6,
            \posRate, Pseg(Pseq([2, 0.2, 4], inf), 4, 'sine' , inf),
            // \posRate, 1,
            \rate, ~pmodenv.(Pseq([0.25, 1], inf), 3),
            // \out, 
            \gain, -12,
            \atk, 0,
            // \dec, 0.1,
            \dec, 0.5,
            \out, [~mainout]
        )
        <> Pbind(\dur, Pseq([Rest(1.5), 1.5], inf))
        // <> Pbind(\dur, Pseq([Rest(1.5), 1.5], inf))
        // <> Pbind(\dur, Pseq([1], inf))
    ));

    sp.wait(sectionLength);

    ///////////////////////////////////////////////
    \h.postln;
    sectionLength = 64;
    // sectionLength = 0;
    Pbindef(\sub, \dec, Pkey(\dur) * 3).quant = 0.0;
    Pbindef(\sub, \freq, Pseq([37, 34, 34, 34], inf)).quant = 0.0;
    // Pbindef(\perc, \rate, 2).quant = 0.0;
    
    Pdef(\p2,
    	~makeSubdivision.(
    		PlaceAll([1, 1, 1, 1, 1, 1], inf),
    		PlaceAll([4, 4, 4, 4], inf)
    	)
    );

    sp.suspend(drum);
    drum = sp.par(
        Pdef(\drum,
            // Pbind(\out, [~filter]) <>
            Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\sub), Pdef(\snare)], Pseq([4, 2, 5, 4, 5, 2, 4, 2, 3, 4, 2, 5, 4, 2, 4, 2] - 1, inf))
            // Pswitch1([Pdef(\kick), Pdef(\hh), Pdef(\rim), Pdef(\sub),], PlaceAll([4, 1, 2, 1, 2, 4, 2, 3] - 1, inf)) 
            // <> ~filterBeat.(key: Pkey(\cyclecount), beat:[3], reject: 1, mod: 4)
            <> Pdef(\p2)
        )
    );
    
    sp.suspend(chain);
    chain = sp.par(
        Pdef(\perc,
            Pbind(
                \instrument, \segPlayer,
                \amp, Pseq([0.8, 0.3, 0.3, 0.8, 0.3, 0.3, 0.8, 0.3, 0.3, 0.8, 0.3, 0.3, 0.8, 0.3, 0.3, 0.3], inf),
                // \amp, 1,
                \atk, 0,
                \rel, Pwhite(0.125, 0.3, inf),
                \curve, -8,
                \buf, ~sliceBuf5.at(\file),
                \rate, 2,
                \oneshot, 0,
                \gain, -6,
                \sliceStart, 0,
                \slice, ~pGetSlice.(Pseries(1, 1, inf).wrap(0, 32) + Pkey(\sliceStart), ~sliceBuf5),
                \pan, ~pmodenv.(Pwhite(0.55, -0.55, inf), 0.25),
                \pitchMix, 0.5,
                \pitchRatio, 1,
                \windowSize, 0.01,
                \pitchDispersion, 0.01,
                \timeDispersion, 0.1,
                \out, [~convolve_B, ~filter]
            )
            <> Pbind(\dur, Pseq([0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.125, 0.125], inf)).withSwing(0.25, 0.005)
        )
    );
    
    sp.suspend(serum);
    serum = sp.par(
        Ppar([
            Pbind(
                \type, \vst_midi,
                \vst, ~serumVst,
                \midicmd, \noteOn,
                \chan, 0,
                \amp, 0.5,
                \midinote, 15,
            )
            <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 2], skew: [-0.5], curve: \exp)
            <> Pdef(\p2)
            ,
    
            Pbind(
                \type, \vst_set,
                \vst, ~serumVst,
                //macro 1
                218, 0.1,
                // 218, Pwhite(0.24, 0.9, inf),
                // 219, 0.1,
                219, Pwhite(1, 0.2, inf),
                // 220, 0,
                220, Pseg(Pseq([0, 1], inf), 2, repeats: inf),
                //atk
                35, 0.01,
                37, 0.4,
                \dur, 0.25
            )
        ])
    );

    sp.suspend(bridge);
    bridge = sp.par(
        Pdef(\bridge,
            Pmono(
                \grainSlicer_mono,       
                \amp, 1,
                \buf, ~sliceBuf.at(\file),
                \sliceStart, 100,
                //lowest to highest
                \overlap, 200,
                \trigRate, 10,
                // \trigRate, Pseg(Pseq([1000, 100], inf), 8, 'exp' , inf),
                // \slice, 10,
                \slice, ~pGetSlice.(
                    (Pseries(1, 1, inf).wrap(0, 4) + Pkey(\sliceStart)).stutter(8), 
                    ~sliceBuf
                ),
                // \gain, 6,
                \posRate, 0.5,
                \rate, 2,
                // \out, 
                \gain, -6,
                \atk, 1,
                // \dec, 0.1,
                \dec, 2,
                \out, [~mainout, ~convolve_B]
            )
            <> Pdef(\p1)
        )
    );

    sp.wait(sectionLength);
    ///////////////////////////////////////////////
    \i.postln;
    sectionLength = 64;
    sp.suspend(drum);
    sp.suspend(chain);
    sp.suspend(serum);
    serum = sp.par(
        Ppar([
            Pbind(
                \type, \vst_midi,
                \vst, ~serumVst,
                \midicmd, \noteOn,
                \chan, 0,
                \amp, 0.25,
                \midinote, 15,
                \gain, -6
            )
            // <> ~pSkew.(Pdef(\p2), key: Pkey(\eventcount), group: [1, 2], skew: [-0.5], curve: \exp)
            <> Pdef(\p2)
            ,
    
            Pbind(
                \type, \vst_set,
                \vst, ~serumVst,
                //macro 1
                // 218, Pseg(Pseq([0, 0.1], inf), 8, repeats: inf),
                218, 0.18,
                // 218, Pwhite(0.24, 0.9, inf),
                219, 0.9,
                // 219, Pwhite(1, 0.2, inf),
                // 220, 1,
                220, Pseg(Pseq([0, 1], inf), 4, repeats: inf),
                //atk
                35, 0.2,
                37, 0.4,
                \dur, 0.1
            )
        ])
    );

    sp.wait(sectionLength);
    sp.suspendAll();
})
).play(t);
)