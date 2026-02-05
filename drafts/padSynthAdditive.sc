(
    var initHarmonicsChain = { |numPartials=10, sidebands=5, freq=80|
        (
            freq: freq,
            //here num partials is number of harmonic nodes
            numPartials: numPartials,
            ratios: (1..numPartials),
            amps: 1 ! numPartials,
            //should be odd but sounds cool either way
            sidebands: sidebands
        )
    };

    var removeNyquistPartials = {|chain|
        var nyquestIdx = chain[\freqs].selectIndices({|item, i| (item >= (s.sampleRate)) || (item <= 0.0)});
        chain[\amps].putEach(nyquestIdx, 0);
        chain;
    };

    var addLimiter = { |chain|
        var nyquist = SampleRate.ir * 0.5;
        var fadeStart = nyquist - 2000;
        var limiter = (1 - (chain[\freqs] - fadeStart) / 1000).clip(0, 1);
        chain[\amps] = chain[\amps] * limiter;
        chain;
    };

    var evenOddMask = {|chain, oddLevel, evenLevel|
        chain[\amps] = chain[\amps].collect { |item, i| if(i.odd){ item * oddLevel; } { item * evenLevel; } };
        chain;
    };

    // exponential/gaussian-ish function bounded 0-1
    var hprofile = {|fi, bwi|
        x = fi/bwi;
        x = x * x;
        x = exp((x.neg) / bwi);
        x;
    };

    // var hprofile = {|fi, bwi, alpha = 1, beta = 3|
    //     var x = (fi / bwi).abs.clip(0, 1);
    //     (x ** (alpha - 1)) * ((1 - x) ** (beta - 1));
    // };

    // var hprofile = {|fi, bwi, exponent = 0.1|
    //     var x = (fi / bwi).abs.clip(0, 1);
    //     (1 - x).pow(exponent);
    // };

    var padSynthDistribution = { |chain, harmonicRatio=1, bw=1, bwScale=1, bwSkew=1, stretch=1|
		//get harmonic integers
        var amps;
		var powN = pow(chain[\ratios], harmonicRatio / 2);
		var relF = (powN * ((1.0 + (powN - 1)) * stretch));
        var bw_Hz, bwi, fi;
        
        //loop vars
        var sidebands = chain[\sidebands];
        var idxOffset = (sidebands / 2).floor;
        var newPartials = List.new();
        var newAmps = List.new();
        var newSize;
        
		//harmonic frequency
		chain[\freqs] = (relF * chain[\freq]);
        bw_Hz = (pow(2, (bw / 1200) - bwSkew)) * chain[\freq] * pow(relF, bwScale);
        bwi = 1 / (chain[\ratios]);
        // bwi = (chain[\freq] * bw_Hz)/(2*s.sampleRate);

        //for each harmonic, create n-1 sidebands with amplitudes of each sideband on gaussianish distribution
        chain[\numPartials].do({|i|
            sidebands.do({ |j|
                var partialIdx = j - idxOffset;
                var freqOffset = bw_Hz[i] / sidebands;
                var subPartialFreq = chain[\freqs][i] + (partialIdx * freqOffset);
                var subPartialAmp = hprofile.(partialIdx.abs.linlin(0, idxOffset, 0, 1), bwi[i]);
                if(subPartialAmp > 0.0){
                    //debug
                    // ['harmonic group ' ++ i, subPartialFreq, subPartialAmp].postln;
                    newPartials.add(subPartialFreq);
                    newAmps.add(subPartialAmp);
                };
            });
        });

        //update dict with new partial
        newSize = newPartials.size;
        chain[\numPartials] = newSize;
        chain[\freqs] = newPartials.asArray;
        chain[\ratios] = (1..newSize);
        chain[\amps] = newAmps.asArray;
        chain;
    };

    {
        var chain, sig, freqDrift, ampDrift;
        
        // 10 harmonics 4 sidebands per harmonic
        chain = initHarmonicsChain.(numPartials: 10, sidebands: 5, freq: 440);
        // 5 harmonics 14 sidebands per harmonic
        // chain = initHarmonicsChain.(numPartials: 8, sidebands: 12, freq: 60);
        //etc
        // chain = initHarmonicsChain.(20, 10, 80);

        //try changing params
        chain = padSynthDistribution.(
            chain, 
            harmonicRatio: 1, 
            bw: 1000,
            bwScale: 1, 
            bwSkew: 1,
            stretch: 1,
        );
        
        chain = addCombFilter.(chain, combOffset, combDensity, combSkew, combPeak);

        chain[\freqs].postln;

        chain = evenOddMask.(chain, 1, 1);
        chain = addLimiter.(chain);
        // chain = removeNyquistPartials.(chain);
        
        //add partial drift
        freqDrift = LFNoise2.ar(4 ! chain[\numPartials]) * 1;
        ampDrift = LFNoise2.ar(0.5 ! chain[\numPartials]) * 0.2;

        sig = SinOsc.ar(
            freq: chain[\freqs],
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]),
            mul: chain[\amps]
        );

        // sig = sig.sum ! 2;
        sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);
        sig = sig * -25.dbamp;

    }.play;
)

5.1664206328379e-55