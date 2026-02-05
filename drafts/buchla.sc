(
    ~mainout = 0;
    ~longverb = Bus.audio(s,2);
    ~nhverb = Bus.audio(s,2);
    ~miVerb = Bus.audio(s,2);
    ~delay = Bus.audio(s,2);
    ~chorus = Bus.audio(s,2);
    
    ~modDelay=Bus.audio(s,2);
    ~mod1_in = Bus.audio(s, 2);
    ~mod1_out = Bus.control(s, 1);
    
    ~modal=Bus.audio(s,2);
    ~formant=Bus.audio(s,2);
    ~grain=Bus.audio(s,2);
    ~tape=Bus.audio(s,2);
    ~pitchShift=Bus.audio(s,2);
    ~filter=Bus.audio(s,2);
    ~eq=Bus.audio(s,2);
    
    ~bufA = Buffer.alloc(s, s.sampleRate * 0.1);
    ~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
    ~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
    
    ~convolve_A=Bus.audio(s,2);
    ~convolve_B=Bus.audio(s,2);
    
    ~morph_A=Bus.audio(s,2);
    ~morph_B=Bus.audio(s,2);
    
    //fx routing
    Pdef(\modal, Pmono(\padKlank, \inbus, ~modal, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\formant, Pmono(\formantBank, \inbus, ~formant, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\grain, Pmono(\liveGrain_mono, \inbus, ~grain, \bufnum, ~bufA, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\tape, Pmono(\tape, \inbus, ~tape, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\pitchShift, Pmono(\pitchShift, \inbus, ~pitchShift, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\filter, Pmono(\vasem12, \inbus, ~filter, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\eq, Pmono(\EQstack, \inbus, ~eq, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    
    Pdef(\morph,
        Pmono(\cepstralMorph_fx,
            \amp, 1,
            \inbus_A, ~convolve_A,
            \inbus_B, ~convolve_B,
            \addAction, \addToTail,
            \callback, { Pdefn(\fxid, ~id) },
        )
    );
    
    Pdef(\nhverb,
        Pmono(\nhverb,
            \amp, 1,
            // \dur, 0.01,
            \inbus, ~nhverb,
            \addAction, \addToTail,
            \gain, 0,
            \callback, { Pdefn(\fxid, ~id) },
        )
    );
    
    Pdef(\miVerb,
        Pmono(\miVerb,
            \amp, 1,
            // \dur, 0.01,
            \inbus, ~miVerb,
            \addAction, \addToTail,
            \gain, 0,
            \callback, { Pdefn(\fxid, ~id) },
        )
    );
)

(
    ~specBuff=Dictionary();
    ~specBuff2=Dictionary();
    // ~makeSpec.("/Users/aelazary/Desktop/Samples etc./NEW sample lib/mother and daughter singing o magnum mysterium-sfLDOVcK7nU.wav", ~specBuff, 16384, 2)
    ~makeSpec.("/Users/aelazary/Desktop/Samples etc./spannerGuitar/spannerDrumGuitar-2.wav", ~specBuff, 16384, 2);
 )
    
(
Synth(\fftStretch_mono,
    [buf: ~specBuff.at(\file), analysis: ~specBuff.at(\analysis), fftSize: ~specBuff.at(\fftsize), rate: 1, pos: 0, len: 0.05, out: ~mainout])
)

(
    Pmono(\fftStretch_mono,
        \amp, 1,
        \gain, 0,
        \buf, ~specBuff.at(\file),
        \analysis, [~specBuff.at(\analysis)],
        \fftSize, ~specBuff.at(\fftSize),
        \rate, 1,
        \pos, 0,
        \len, 0.05,
        \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine)
    ).play(t);
)

    // Test
(
    t = TempoClock.new(140/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});
    Pdef(\player,
        Pspawner({| sp |
            var sectionLength = 32;

    Pdef(\mod, 
        Pmono(\west,
            \dec, Pkey(\dur) * 1,
            \freq, 40,
            \gate, 1,
            
            // \pitchBendRatio, Pwhite(0.5, 2),
            \glide, 0.01, 
                
            \fm1Ratio, 4, 
            \fm2Ratio, 3,
            \fm1Amount, 0.1, 
            \fm2Amount, 0.1,
            
            \vel, 0.5, 
            \pressure, ~pmodenv.(Pkey(\groupdelta) * 5, Pkey(\dec)), //Pwhite(), 
            // \timbre, Pwhite(0.0,0.75), 
            \timbre, ~pmodenv.(Pwhite(0, 1), Pkey(\dec)),
            \waveShape, 0.25, 
            \waveFolds, ~pmodenv.(1 - Pkey(\groupdelta), Pkey(\dec)), 
            \envType, ~pmodenv.(Pseq([0, 1], inf), 4), 
            // \peak, ~pmodenv.(Pwhite(250.0, 4000.0), Pkey(\dec)),
            \peak, ~pmodenv.(Pwhite(250.0, 15000.0), Pkey(\dec)),
            \decay, Pwhite(1, 2),
            // \decay, 1,
            \pan, Pbrown(-0.5,0.5,0.001),
            \amp, 0.5,
            \lfoShape, 0, //Pwhite(), 
            \lfoFreq, Pkey(\dur),
            // \lfoFreq, Pwhite(0.1, 5.0),
            \lfoToWaveShapeAmount, ~pmodenv.((1 - Pkey(\groupdelta)) * 0.25, Pkey(\dec)),
            \lfoToWaveFoldsAmount, ~pmodenv.(Pkey(\groupdelta) * 1, Pkey(\dec)),

            \lfoToReverbMixAmount, Pwhite(), 
            \drift, ~pmodenv.(Pwhite(0, 0.1), Pkey(\dec))
        )
    );
            
        Pdef(\p1,
            ~makeSubdivision.(
                PlaceAll([2.5, 1.5, 1.5, Rest(1)], inf),
                PlaceAll([[8, 3], 4, 2, 1, 4], inf)
            )
        );
        
        sp.par(
            Pdef(\west,
                Pdef(\mod) <>
                // ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 2, 4], mod: 3) <>
                ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), group: [1, 2], skew: [2, -1], curve: \exp) <> 
                Pdef(\p1)
                <> Pbind(
                    \instrument, \west,
                    \out, [~convolve_B, ~mainout]
                )
            )
        );
        
        sp.par(
        Pmono(\fftStretch_mono,
            \amp, 1,
            \gain, 0,
            \buf, ~specBuff.at(\file),
            \analysis, [~specBuff.at(\analysis)],
            \fftSize, ~specBuff.at(\fftSize),
            \rate, 1,
            \pos, 0.3,
            \len, 0.1,
            \filter, ~pmodenv.(Pseq([1, 4],inf), Pseq([4, 8, 4], inf), curve: \sine),
            \out, [~mainout, ~convolve_A]
        ));
        
        sp.par(
			Pbindf(
				Pdef(\morph),
				\gain, -6,
				\atk, 0,
				\rel, 100,
				\swap, ~pmodenv.(Pseq([0, 1],inf), Pseq([4], inf)),
				// \swap, 1,
				\out, [~mainout, ~miVerb]
			).finDur(sectionLength)
		);
	})
    ).play(t);
)