(
SynthDef(\input, {
    var sig;
    sig = SoundIn.ar(\in.kr(0)!2);
    sig = sig * \gain.kr(1);
	sig = sig * \amp.kr(1);
    Out.ar(\out.kr(0), sig);
}).add;
)

// {SinOsc.ar(440!2)*0.1}.play
Synth(\input, [\in, 1])

(
    ~sliceBuf = Dictionary();
    ~sliceBuf2 = Dictionary();
    ~sliceBuf3 = Dictionary();
    ~sliceBuf4 = Dictionary();
    ~sliceBuf5 = Dictionary();
    ~sliceBuf6 = Dictionary();
    ~sliceBuf7 = Dictionary();
    
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/sventojibridgecontact07261150.wav", ~sliceBuf, 0.1, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/RomssavahjohkaUnderBridge.wav", ~sliceBuf2, 0.2, \flatness);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/sventojibridgecontact07261150.wav", ~sliceBuf3, 0.2, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Field recs/Rock song.wav", ~sliceBuf4, 0.2, \centroid);    
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/kusnieriunaiwatetower.wav", ~sliceBuf5, 0.2, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/cordedamarageportdajaccioLR.wav", ~sliceBuf6, 0.2, \centroid);
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Drum Loops/Silent Hill 2/Block Mind/loop 9 0013.wav", ~sliceBuf7, 0.2, \centroid);
)

(
    b = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./NEW sample lib/1030 Rave Stabs/100 percent.wav");
    c = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./EchoThiefImpulseResponseLibrary/Brutalism/GeiselLibrary.wav");
    d = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./EchoThiefImpulseResponseLibrary/Miscellaneous/MillsArtMuseum.wav");
)

// t = TempoClock.new(140/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

Pdef(\player).clear;
Pdef(\player).fadeTime = 8;
Pbindef(\player).play(t);
Pdef(\player).stop;

(
Pdef(\player,
Pspawner({| sp |
    var sectionLength;
    		//fx const
		sp.par(
			Pbindf(
				Pdef(\miVerb),
				\time, 0.6,
				\damp, 0.6,
				\hp, 0.0,
				\freeze, 0,
				\diff, 0.9,
				\gain, -24,
                \dur, 0.1,
				\out, ~mainout
			)
		);
        
        sp.par(
            Pdef(\mic1,
                Pmono(\input,
                    \in, 2,
                    \amp, ~slider.(0).lincurve(0,1,0,1,4),
                    \out, [~convolve_B],
                    \dur, 0.1,
                )
                
            )
        );

        sp.par(
            Pdef(\grainplayer,
                Pmono(
                    \grainSlicer_mono,  
                    \dur, 0.1,     
                    \amp, ~slider.(2).lincurve(0,1,0,1,-4),
                    \buf, ~sliceBuf.at(\file),
                    \sliceStart, ~knob.(0).linlin(0,1,0,200).floor,
                    //lowest to highest
                    \overlap, ~knob.(1).linlin(0,1,10,500),
                    \trigRate, ~knob.(2).linexp(0,1,100,10000),
                    // \trigRate, Pseg(Pseq([1000, 100], inf), 8, 'exp' , inf),
                    \slice, ~pGetSlice.(
                        Pkey(\sliceStart),
                        ~sliceBuf
                    ),
                    \posRate, ~knob.(3).linlin(0,1,0,2),
                    \rate, ~knob.(4).linlin(0,1,0.5,2),
                    // \out, 
                    \gain, 0,
                    \atk, 1,
                    \dec, 2,
                    \polarityMod, ~knob.(5) - 0.01,
                    \out, [~convolve_A]
                )
            )
        );

        sp.par(
            Pmono(\padKlank_mono,
                \dur, 0.1,
                \f, 30,
                \harmonicRatio, 0.75,
                \bw, 1,
                \bwScale, ~slider.(4).linlin(0,1,0,3),
                \stretch, 1,
                \detuneRate, 800,
                \detuneDepth, 20,
                \impulse, 20000,
                \dev, 1,
                \amp, ~slider.(3).lincurve(0,1,0,1,4),
                \gain, -40,
                \out, [~convolve_B, ~miVerb]
            )
        );

		sp.par(
			Pbindf(
				Pdef(\morph),
                \dur, 0.1,
				\gain, 0,
				\atk, 0,
				\rel, 10000,
                \drywet, ~knob.(7),
				// \swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([4], inf)),
				\swap, ~slider.(1).lincurve(0,1,0,1,-4),
				\out, [~pitchShift]
			)
		);

        sp.par(
			Pbindf(
				Pdef(\pitchShift),
                \dur, 0.1,
                \pitchRatio, 0.5,
                \windowSize, ~slider.(5),
                \pitchDispersion, ~slider.(6),
                \timeDispersion, 0.01,
                \drywet, ~knob.(7),
                \gain, 0.0,
				\out, [~mainout, ~miVerb]
			)
		);
})
)
)

(
// Pdef(\player).stop;
Pdef(\player,
Pspawner({| sp |
    var sectionLength;
    		//fx const
		sp.par(
			Pbindf(
				Pdef(\miVerb),
                \dur, 0.1,
				\time, 0.6,
				\damp, 0.6,
				\hp, 0.0,
				\freeze, 0,
				\diff, 0.9,
				\gain, -24,
				\out, ~mainout
			)
		);
        
        sp.par(
            Pdef(\mic1,
                Pmono(\input,
                    \in, 2,
                    \amp, ~slider.(0).lincurve(0,1,0,1,-4),
                    \out, [~convolve_B],
                    \dur, 0.1
                )
            )
        );

        sp.par(
            Pmono(\padKlank_mono,
                \dur, 0.1,
                \f, 15,
                \harmonicRatio, 0.75,
                \bw, 1,
                \bwScale, 1,
                \stretch, 1,
                \detuneRate, 800,
                \detuneDepth, 1,
                \impulse, 20000,
                \dev, 1,
                \amp, ~slider.(2).lincurve(0,1,0,1,-4),
                \gain, -24,
            )
        );

        sp.par(
			Pbindf(
				Pmono(\convolved_pulsar_mono),
                \dur, 0.1,
				\buf, b,
				\window, 4096,
				\triggerRate, ~knob.(3).linlin(0,1,3,20),
				\fluxMF, 1.5,
				\fluxMD, 1,
				\grainFreq, 300,
				\overlap, 1,
				\pmRatio, 2,
				\pmIndex, 100,
				\density, ~knob.(4) - 0.01,
				\polarityMod, 1,
				\gain, 0,
                \amp, ~slider.(3).lincurve(0,1,0,1,-4),
				\out, [~convolve_A]
			)
		);

        Pdef(\fb1mod,
            Pbind(
                \dur, 0.1,
                \amp, ~slider.(4).lincurve(0,1,0,0.5,-4),
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
                // \gain, -12,
            )
        );

        sp.par(
            Pbindf(
                Pdef(\seq3,
                    // Pbind(\lag, 16)
                    Pmono(\fb1_mono)
                    <> Pdef(\fb1mod) 
                ),
                \out, [~convolve_A, ~miVerb]
            )
        );

		sp.par(
			Pbindf(
				Pdef(\morph),
                \dur, 0.1,
				\gain, 0,
				\atk, 500,
				\rel, 100,
				\swap, ~slider.(1).linexp(0,1,0.1,1),
                \drywet, ~knob.(7),
				\out, [~pitchShift]
			)
		);

        sp.par(
			Pbindf(
				Pdef(\pitchShift),
                \dur, 0.1,
                \pitchRatio, 2.0,
                \windowSize, ~knob.(5),
                \pitchDispersion, ~slider.(6),
                \timeDispersion, ~slider.(7),
                \drywet, ~knob.(6),
                \gain, 0.0,
				\out, [~mainout, ~miVerb]
			)
		);
})
)
)

(
Pdef(\player,
Pspawner({| sp |
    var sectionLength;
        //fx const
        sp.par(
            Pbindf(
                Pdef(\miVerb),
                \time, 0.6,
                \damp, 0.6,
                \hp, 0.0,
                \freeze, 0,
                \diff, 0.9,
                \gain, 0,
                \dur, 0.1,
                \out, ~mainout
            )
        );
        
        sp.par(
            Pdef(\mic1,
                Pmono(\input,
                    \in, 2,
                    \amp, ~slider.(0).lincurve(0,1,0,1,-4),
                    \dur, 0.1,
                    \out, [~convolve_B],
                )
            )
        );

        Pdef(\aMod,
            Pbind(
                \instrument, \pulsePluck,
                // \scale, Scale.minor.tuning_(\just),
                \scale, Scale.minor,
                \root, -4, 
                \ctranspose, 1,x
                \mtranspose, ~knob.(0).linlin(0,1,-12,12).floor,
                // \degree, (Pkey(\eventcount).stutter(3) - Pkey(\groupcount).stutter(3)),
                \degree, (Pkey(\eventcount).stutter(2) - (Pkey(\groupcount).stutter(3))),
                // \degree, (Pkey(\eventcount).stutter(3) - Pkey(\groupcount).stutter(2)),
                \octave, Pkey(\groupcount).wrap(1,3) + 3,
                \detune, 0.5,
                \envDepth, 100,
    
                \atk, 1,
                \dec, 0.4,
                \sus, 0.1,
                \rel, 4,
                
                \filterAtk, 0.01,
                \filterDec, ~slider.(3).lincurve(0,1,0,1,-4),
                \pitchEnv, 0,
    
                \driftRate, 2.0,
                \drift, 1.0,
                \lfoShape, ~knob.(1).linlin(0,1,0,3).floor,
                \lfoFreq, ~knob.(2).linexp(0,1,0.1,200),
    
                \lfoToWidth, ~knob.(3),
                \lfoToShapeAmount, ~knob.(4) * 4,
                \lfoToFilter, ~knob.(5) * 4,
                \lfoToMorph, 1,
    
                \filter, 25.0,
                \filter2, 5000.0,
                \width, 1,
                \shape, 0,
                \morph, 0.5,
                \gain, -6.0,
                \amp, ~slider.(1).lincurve(0,1,0,1,-4),
                \pan, ~pmodenv.(Pwhite(-0.25, 0.25), Pkey(\dur), 1, \sin),
                // pan, -1,
                \out, [~tape]
            )
        );

        Pdef(\bMod,
            Pbind(
                // \scale, Scale.minor.tuning_(\just),
                \scale, Scale.minor,
                \root, -4,
                \mtranspose, ~knob.(0).linlin(0,1,-12,12).floor,
                \degree, (Pkey(\eventcount).stutter(3) - Pkey(\groupcount).stutter(3)) + 4,
                // \degree, (Pkey(\eventcount).stutter(2) - (Pkey(\groupcount).stutter(3))) + 4;,
                // \degree, (Pkey(\eventcount).stutter(3) - Pkey(\groupcount).stutter(2)) + 4,
                \octave, Pkey(\groupcount).wrap(1, 3) + 3,
                \detune, 0.5,
                \envDepth, 0,
    
                \atk, 1,
                \dec, 0.4,
                \sus, 0.1,
                \rel, 4,
    
                \filterAtk, 0.01,
                \filterDec, ~slider.(3).lincurve(0,1,0,1,-4),
    
                \driftRate, 2.0,
                \drift, 1.0,
                \lfoShape, ~knob.(1).linlin(0,1,0,3).floor,
                \lfoFreq, ~knob.(2).linexp(0,1,0.1,200),
    
                \lfoToWidth, ~knob.(3),
                \lfoToShapeAmount, ~knob.(4) * 4,
                \lfoToFilter, ~knob.(5) * 4,
                \lfoToMorph, 1,
    
                \filter, 25.0,
                \filter2, 5000.0,
                \width, 0.5,
                \shape, 1,
                \morph, 0.5,
                \gain, -6.0,
                \amp, 1.0,
                \out, 0.0,
                // \pan, ~pmodenv.(Pwhite(-0.8, 0.8), Pkey(\dur), 1, \sin),
                // \pan,
                \out, [~tape]
            )
        );
    
        Pdef(\p1,
            ~makeSubdivision.(
                PlaceAll([Prand([1.5, 1], inf), 2, 1, 1] * 2, inf),
                PlaceAll([2, [4, 2], 1, 2, 2], inf)
            )
        );

        Pdef(\p2,
            ~makeSubdivision.(
                PlaceAll([Prand([1.5, 1], inf), 2, 6] * 0.5, inf),
                PlaceAll([2, [4, 2], 0], inf)
            )
        );

        sp.par(
            Pbindf(
                Pdef(\tape),
                \pregain, 0.0,
                \dcOffset, ~slider.(4).lincurve(0,1,0,-0.1,0.1),
                // \dcOffset, 0.,
                \sigmoid,0.2,
                \postgain, -6,
                \autogain, 0,
                \drywet,  ~slider.(2).lincurve(0,1,0,1,-4),
                \amp, 1.0,
                \dur, 0.1,
                \out, [~pitchShift, ~miVerb]
            )
        );
    
        sp.par(
            Pdef(\a,
                Pdef(\aMod)
                <> ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 4])
                <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1], skew: [-0.5], curve: \exp)
                <> Pdef(\p1)
            )
        );

        sp.par(
            Pdef(\b, 
                Pdef(\bMod)
                <> Pdef(\p2)
                <> Pbind(\instrument, \pulsePluck)
            )
        );

		sp.par(
			Pbindf(
				Pdef(\morph),
                \dur, 0.1,
				\gain, 0,
				\atk, 500,
				\rel, 100,
				\swap, ~slider.(1).linexp(0,1,0.1,1),
                \drywet, ~knob.(7),
                \dur, 0.1,
				\out, [~mainout]
			)
		);

        sp.par(
			Pbindf(
				Pdef(\pitchShift),
                \dur, 0.1,
                \pitchRatio, 0.5,
                \windowSize, ~slider.(5),
                \pitchDispersion, ~slider.(6),
                \timeDispersion, ~slider.(7),
                \drywet, ~knob.(6),
                \gain, 0.0,
				\out, [~mainout, ~convolve_A]
			)
		);
})
)
)

(
Pdef(\player,
    Pspawner({| sp |
        var sectionLength;
            //fx const
            sp.par(
                Pbindf(
                    Pdef(\miVerb),
                    \dur, 0.1,
                    \time, 0.6,
                    \damp, 0.6,
                    \hp, 0.0,
                    \freeze, 0,
                    \diff, 0.9,
                    \gain, -24,
                    \out, ~mainout
                )
            );
            
            sp.par(
                Pdef(\mic1,
                    Pmono(\input,
                        \in, 1,
                        \amp, 1,
                        \dur, 0.1,
                        \out, [~mainout, ~pitchShift]
                    )
                )
            );

            Pdef(\fmPad, Pmono(\fmPerc_mono) <> Pbind(
                \freq, 60,
                \amp, 1,
                \dec, Pkey(\dur) * 2,
                \carRatio, Pseq([1, 0.5], inf).stutter(8),
                \m1Ratio, 7,
                \m2Ratio, 2,
                \m3Ratio, 0.5,
                \index1, ~knob.(1).linlin(0,1,0.5,4),
                \index2, ~knob.(2).linlin(0,1,0.5,4),
                \iscale, ~knob.(0).linlin(0,1,0.0001,0.01),
                \feedback, ~knob.(3).linlin(0,1,0.1,0.3),
                \fbmod, ~pmodenv.(Pwhite(0, 1, inf), Pkey(\dec)),
                \spread,~pmodenv.(Pwhite(0, 30, inf), Pkey(\dec)),
                \mix, ~pmodenv.(Pwhite(0.25, 0.75), Pkey(\dec)),
                \drive, ~pmodenv.(Pwhite(0, 6), Pkey(\dec)),
                \gain, 12,
                \out, [~mainout, ~pitchShift]
                )
            );

            Pdef(\fmPad4,
                Pbindf(
                    Pdef(\fmPad),
                    \amp, ~slider.(0).lincurve(0,1,0,10,4),
                    \freq, 15,
                    \carRatio, 0.5,
                    \m1Ratio, 7,
                    \m2Ratio, 26,
                    \m3Ratio, 1.5,
                    \mix, 1,
                    \gain, 30,
                    \dur, 0.1,
                )
            );
        
            Pdef(\fmPad2,
                    Pbindf(
                        Pdef(\fmPad),
                        \amp, ~slider.(1).lincurve(0,1,0,10,4),
                        \freq, 15,
                        \carRatio, 0.5,
                        \m1Ratio, 8,
                        \m2Ratio, 25,
                        \m3Ratio, 2,
                        \mix, 1,
                        \gain, 30,
                        \dur, 0.1,
                    )
            );
            
            Pdef(\fmPad3,
                    Pbindf(
                        Pdef(\fmPad),
                        \amp, ~slider.(2).lincurve(0,1,0,1,4),
                        \carRatio, 0.5,
                        \index1, 0.5,
                        \iscale, 0.01,
                        \m1Ratio, 8,
                        \m2Ratio, 6,
                        \m3Ratio, 0.25,
                        \mix, 1,
                        \gain, 15,
                        \dur, 0.1,
                    )
            );

            sp.par(
                Pdef(\fmPad2)
            );

            sp.par(
                Pdef(\fmPad3)
            );

            sp.par(
                Pdef(\fmPad4)
            );

            sp.par(
                Pbindf(
                    Pdef(\pitchShift),
                    \dur, 0.1,
                    \pitchRatio, 0.5,
                    \windowSize, ~knob.(5),
                    \pitchDispersion, ~slider.(6),
                    \timeDispersion, ~slider.(7),
                    \drywet, ~knob.(6),
                    \gain, 6.0,
                    \out, [~mainout, ~miVerb]
                )
            );
})
)
)

~serumVst = VSTPluginController(Synth(\vsti)).open("Serum");
~serumVst.loadPreset("springs4");

~serumVst.editor(true)
~serumVst.savePreset("springs4");
~serumVst.gui

Pdef(\player).play(t);

(
    ~specBuff=Dictionary();
    ~specBuff2=Dictionary();
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./spannerGuitar/spannerDrumGuitar-4.wav", ~specBuff, 16384, 2);
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./spannerGuitar/spannerDrumGuitar-5.wav", ~specBuff2, 16384, 2);
)

(
Pdef(\player,
Pspawner({| sp |
    sp.par(
        Pbindf(
            Pdef(\miVerb),
            \dur, 0.1,
            \time, 0.6,
            \damp, 0.6,
            \hp, 0.0,
            \freeze, 0,
            \diff, 0.9,
            \gain, 0,
            \out, ~mainout
        )
    );

    sp.par(
        Pdef(\mic1,
            Pmono(\input,
                \in, 4,
                \amp, 1,
                \dur, 0.1,
                \out, [~convolve_B]
            )
        )
    );

    Pdef(\p1,
        ~makeSubdivision.(
            PlaceAll([1.5, Prand([1, 2], inf), 1], inf),
            PlaceAll([4, 4, 4], inf)
        )	
    );

    sp.par(
        Ppar([
            Pbind(
                \type, \vst_midi,
                \vst, ~serumVst,
                \midicmd, \noteOn,
                \chan, 0,
                \midinote, 15,
                \amp, ~slider.(0).lincurve(0,1,0,1,8),
                \out, [~convolve_B]
            )
            <> ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2], skew: [1], curve: \exp)
            <> Pdef(\p1)
            ,

            Pbind(
                \type, \vst_set,
                \vst, ~serumVst,
                //macro 1
                218, ~knob.(0),
                // 218, Pwhite(0.24, 0.9, inf),
                219, ~knob.(1),
                // 219, Pwhite(0.25, 0.9, inf),
                220, ~knob.(2),
                // 220, Pwhite(0, 1, inf),
                //atk
                35, ~knob.(3),
                37, ~knob.(4),
                44, ~knob.(5),
                \dur, 0.25
            )
        ])
    );

    sp.par(
        Pmono(\fftStretch_magFilter_mono,
            \amp, ~slider.(1).lincurve(0,1,0,1,8),
            \gain, 12,
            \dur, 0.01,
            \buf, ~specBuff.at(\file),
            \analysis, [~specBuff.at(\analysis)],
            \fftSize, ~specBuff.at(\fftSize),
            \rate,  ~knob.(7),
            \pos, ~knob.(6),
            \len, 0.05,
            \thresh, ~slider.(2).linlin(0,1,0,10),
            \remove, ~slider.(3).linlin(0,1,0,100),
            \out, [~convolve_A]
        )
    );

    sp.par(
        Pbindf(
            Pdef(\morph),
            \dur, 0.1,
            \gain, 0,
            \atk, 500,
            \rel, 100,
            \swap, ~slider.(6).linexp(0,1,0.1,1),
            \drywet, ~slider.(7),
            \out, [~mainout]
        )
    );

})
)
)

~bufA = Buffer.alloc(s, s.sampleRate * 0.1);
~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
~bufC = Buffer.alloc(s, s.sampleRate * 0.025);

Pdef(\player).play(t)

(
~bufA = Buffer.alloc(s, s.sampleRate * 0.1);
~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
Pdef(\player,
Pspawner({| sp |
    var sectionLength;
        //fx const
        sp.par(
            Pbindf(
                Pdef(\miVerb),
                \time, 0.6,
                \damp, 0.6,
                \hp, 0.0,
                \freeze, 0,
                \diff, 0.9,
                \gain, 0,
                \dur, 0.1,
                \out, ~mainout
            )
        );

        sp.par(
            Pdef(\mic1,
                Pmono(\input,
                    \in, 0,
                    \amp, ~slider.(2).lincurve(0,1,0,1,-4),
                    \dur, 0.1,
                    \out, [~grain],
                )
            )
        );
        
        sp.par(
            Pdef(\guitar,
                Pmono(\input,
                    \in, 1,
                    \amp, ~slider.(0).lincurve(0,1,0,1,-4),
                    \dur, 0.1,
                    \out, [~grain],
                )
            )
        );

        sp.par(
            Pbindf(
                Pdef(\grain),
                \bufnum, ~bufA,
                \amp, ~slider.(1).lincurve(0,1,0,1,-4),
                \overlap, 20,
                // \overlap, ~pmodenv.(Pseq([5, 10, 20], inf), 20),
                \triggerRate, ~pmodenv.(Pseq([100, 1000, 10000], inf), 6),
                // \triggerRate, 1000,
                // \posRate, ~pmodenv.(Pseq([0, 1], inf), 12),
                \posRate, 0.2,
                \rate, 1,
                \recRate, 1,
                // \polarityMod, 1,
                \polarityMod,~knob.(0),
                \overdub, ~knob.(6),
                \feedback, ~knob.(7).linlin(0,1,0,0.5),

                // \feedback, 0,
                \gain, 0,
                \dur, 0.1,
                \out, [~mainout]
            )
        );

        sp.par(
            Pbindf(
                Pdef(\morph),
                \dur, 0.1,
                \gain, 0,
                \atk, 500,
                \rel, 100,
                \swap, ~slider.(6).linexp(0,1,0.1,1),
                \drywet, ~slider.(7),
                \out, [~mainout]
            )
        );
})
)
)

Pdef(\player).clear
Pdef(\player).play

(
~bufA = Buffer.alloc(s, s.sampleRate * 0.1);
~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
~fftSize = 16384 * 0.5;
~analysisFX = Array.fill(2, {Buffer.alloc(s, ~fftSize)});

    Pdef(\player,
    Pspawner({| sp |
        var sectionLength;
            //fx const
            sp.par(
                Pbindf(
                    Pdef(\miVerb),
                    \time, 0.6,
                    \damp, 0.6,
                    \hp, 0.0,
                    \freeze, 0,
                    \diff, 0.9,
                    \gain, 0,
                    \dur, 0.1,
                    \out, ~mainout
                )
            );

            sp.par(
                Pdef(\mic1,
                    Pmono(\input,
                        \in, 0,
                        \amp, ~slider.(2).lincurve(0,1,0,1,-4),
                        \dur, 0.1,
                        \out, [~convolve_A],
                    )
                )
            );
            
            sp.par(
                Pdef(\guitar,
                    Pmono(\input,
                        \in, 1,
                        \amp, ~slider.(0).lincurve(0,1,0,1,-4),
                        \dur, 0.1,
                        \out, [~fftStretchLive],
    
                    )
                )
            );
    
            sp.par(
                Pbindf(
                    Pdef(\fftStretchLive),
                    \buf, ~bufA,
                    \amp, ~slider.(0),
                    \analysis, [~analysisFX],
                    \fftSize, ~fftSize,
                    \recRate, 1,
                    \len, ~knob.(1),
                    \thresh, ~knob.(2).linlin(0,1,0, 10),
                    \remove, ~knob.(3).linlin(0,1,0,100),
                    \rate,  ~knob.(4),
                    // \pos, ~knob.(5),
                    \pos, 0,
                    \overdub, ~knob.(6),
                    \feedback, 0,
                    \gain, 6,
                    \dur, 0.1,
                    \out, [~mainout]
                )
            );

            sp.par(
                Pbindf(
                    Pdef(\morph),
                    \dur, 0.1,
                    \gain, 0,
                    \atk, 500,
                    \rel, 100,
                    \swap, ~slider.(6).linexp(0,1,0.1,1),
                    \drywet, ~slider.(7),
                    \out, [~mainout]
                )
            );
    })
    )
)