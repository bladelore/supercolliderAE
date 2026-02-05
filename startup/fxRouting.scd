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
    ~roar=Bus.audio(s,2);
    ~fftStretchLive=Bus.audio(s,2);
    
    ~bufA = Buffer.alloc(s, s.sampleRate * 0.1);
    ~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
    ~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
    
    ~convolve_A=Bus.audio(s,2);
    ~convolve_B=Bus.audio(s,2);
    
    ~morph_A=Bus.audio(s,2);
    ~morph_B=Bus.audio(s,2);
    ~sineTracker=Bus.audio(s,2);
    ~multiband=Bus.audio(s,2);

    
    //fx routing
    Pdef(\modal, Pmono(\padKlank, \inbus, ~modal, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\formant, Pmono(\formantBank, \inbus, ~formant, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\grain, Pmono(\liveGrain_mono, \inbus, ~grain, \bufnum, ~bufA, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\tape, Pmono(\tape, \inbus, ~tape, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\pitchShift, Pmono(\pitchShift, \inbus, ~pitchShift, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\filter, Pmono(\vasem12, \inbus, ~filter, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\eq, Pmono(\EQstack, \inbus, ~eq, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\roar, Pmono(\roar, \inbus, ~roar, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) },));
    Pdef(\fftStretchLive, Pmono(\fftStretchLive_mono, \inbus, ~fftStretchLive, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    Pdef(\sinetracker, Pmono(\sineTracker_fx, \inbus, ~sineTracker, \addAction, \addToTail, \amp, 1, \callback, { Pdefn(\fxid, ~id) }));
    
    Pdef(\delay,
        Pmono(\echo,
            \amp, 1,
            \inbus, ~delay,
            \addAction, \addToTail,
            \callback, { Pdefn(\fxid, ~id) },
        )
    );

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

    Pdef(\follower1,
        Pmono(\envfollow,
            \amp, 1,
            \dur, 0.1,
            \atk, 0.01,
            \rel, 0.1,
            \amp, 1,
            \mul, 0.001,
            \add, 0.01,
            \inbus, ~mod1_in,
            \out, ~mod1_out,
            \callback, { Pdefn(\fxid, ~id) },
        )
    );

    Pdef(\multiband,
        Pmono(\multiBandComp,
            \amp, 1,
            \inbus, ~multiband,
            \addAction, \addToTail,
            \callback, { Pdefn(\fxid, ~id) },
        )
    );

    ~spectralGrains=Bus.audio(s,2);

    Pdef(\spectralGrains1,
        Pmono(\spectralGrains1,
            \amp, 1,
            \inbus, ~spectralGrains,
            \addAction, \addToTail,
            \callback, { Pdefn(\fxid, ~id) },
        )
    );

    ~resonator=Bus.audio(s,2);

    Pdef(\resonator,
        Pmono(\resonator,
            \amp, 1,
            \inbus, ~resonator,
            \addAction, \addToTail,
            \callback, { Pdefn(\fxid, ~id) },
        )
    );
)