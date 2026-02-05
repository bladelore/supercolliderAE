(
    var initChain = { |numPartials, freq|
        (
            freq: freq,
            numPartials: numPartials,
            ratios: (1..numPartials),
            amps: 1 ! numPartials,
        )
    };

    var makeStretchedHarmonicSeries = { |chain, inharmonicity|
        chain[\freqs] = chain[\freq] * chain[\ratios] * (1 + (inharmonicity * chain[\ratios] * chain[\ratios])).sqrt;
        chain;
    };

    var makeStretchedHarmonicSeries2 = { |chain, harmonicRatio, ampScale, ampSkew, stretch|
		//get harmonic integers
        var amps;
		var powN = pow(chain[\ratios], harmonicRatio / 2);
		var relF = (powN * ((1.0 + (powN - 1)) * stretch));
		//harmonic frequency
		chain[\freqs] = (relF * chain[\freq]) ;
		//scale amp and skew
        amps = ((pow(2, ampScale) - ampSkew) * pow(relF, ampSkew));
        chain[\amps] = amps;
    };

    var evenOddMask = {|chain, oddLevel, evenLevel|
        chain[\amps] = chain[\amps].collect { |item, i| if(i.odd){ item * oddLevel; } { item * evenLevel; } };
        chain;
    };

    var removeNyquistPartials = {|chain|
        chain[\amps] = chain[\amps] * (chain[\freqs] <= (s.sampleRate * 0.5));
        chain;
    };
    
    var transferFunc = { |phase, skew|
        Select.kr(phase > skew, [
            0.5 * phase / skew,
            0.5 * (1 + ((phase - skew) / (1 - skew)))
        ]);
    };
    
    var warpFrequencies = { |chain, warpPoint|
        var normalizedFreqs = (chain[\freqs] / chain[\freqs][chain[\numPartials] - 1]).clip(0, 1);
        var warpedFreqs = transferFunc.(normalizedFreqs, 1 - warpPoint);
        chain[\freqs] = warpedFreqs * chain[\freqs][chain[\numPartials] - 1];
        chain;
    };
    
    var addSpectralTilt = { |chain, tiltPerOctave|
        chain[\amps] = chain[\amps] * (chain[\ratios].log2 * tiltPerOctave).dbamp;
        chain;
    };
    
    var raisedCos = { |phase, index|
        var cosine = cos(phase * 2pi);
        var raised = exp(index.abs * (cosine - 1));
        var hanning = 0.5 * (1 + cos(phase * 2pi));
        raised * hanning;
    };
    
    var addCombFilter = { |chain, combOffset, combDensity, combSkew, combPeak|
        var phase, warpedPhase;
        phase = chain[\freqs].log2 - chain[\freq].log2;
        phase = (phase * combDensity - combOffset).wrap(0, 1);
        warpedPhase = transferFunc.(phase, combSkew);
        chain[\amps] = chain[\amps] * raisedCos.(warpedPhase, combPeak);
        chain;
    };
    
    var addLimiter = { |chain|
        var nyquist = SampleRate.ir / 2 - 2000;
        var fade = nyquist - 1000;
        var limiter = 1 - ((chain[\freqs].clip(fade, nyquist) - fade) * 0.001);
        chain[\amps] = chain[\amps] * limiter;
        chain;
    };

    var addNotchFilter = { |chain, notchFreq, notchWidth|
        var freqs = chain[\freqs];
        var amps = chain[\amps];
        
        // Compute attenuation factor based on frequency proximity to notchFreq
        amps = amps.collect { |amp, i|
            var dist = (freqs[i] - notchFreq).abs;
            var attenuation = 1.0 - exp(dist.neg / notchWidth);
            amp * attenuation;
        };
        
        chain[\amps] = amps;
        chain;
    };

    var generateFormants = { |chain, formants|
        var freqs = chain[\freqs];
        var amps = chain[\amps];
        
        // Initialize the resulting amplitudes
        amps = amps.collect { |amp, i|
            var freq = freqs[i];
            // Sum contributions from all formants
            var formantContribution = formants.collect { |formant|
                var centerFreq = formant[\freq];
                var bandwidth = formant[\bandwidth];
                var strength = formant[\strength];
                // Gaussian distribution for smooth formant shape
                strength * exp(((freq - centerFreq).squared).neg / (2 * (bandwidth.squared)))
            }.sum;
            amp * formantContribution
        };
        
        chain[\amps] = amps;
        chain;
    };
    
    
    SynthDef(\additiveComb, {
    
        var numPartials = 128;
    
        var lfos, combOffset, combDensity, combPeak, combSkew, warpSpectrum, inharmonicity, freq, chain, sig;
        var harmonicRatio, ampScale, ampSkew, stretch, oddLevel, evenLevel;
        var partialDrift, partialDriftFreq, partialDriftMD;
        var phaseMD;

        lfos = 6.collect{ |i|
            SinOsc.kr(\modMF.kr(0.5, spec: ControlSpec(0.1, 3)), Rand(0, 2pi));
        };
    
        combOffset = \combOffset.kr(0, spec: ControlSpec(0, 1));
        combOffset = combOffset * (2 ** (lfos[0] * \combOffsetMD.kr(0, spec: ControlSpec(0, 2))));
    
        combDensity = \combDensity.kr(0, spec: ControlSpec(0, 1));
        combDensity = combDensity * (2 ** (lfos[1] * \combDensityMD.kr(0, spec: ControlSpec(0, 2))));
    
        combPeak = \combPeak.kr(5, spec: ControlSpec(1, 10));
        combPeak = combPeak * (2 ** (lfos[2] * \combPeakMD.kr(0, spec: ControlSpec(0, 2))));
    
        combSkew = \combSkew.kr(0.5, spec: ControlSpec(0.01, 0.99));
        combSkew = combSkew * (2 ** (lfos[3] * \combSkewMD.kr(0, spec: ControlSpec(0, 2))));
    
        warpSpectrum = \warpSpec.kr(0.5, spec: ControlSpec(0, 1));
        warpSpectrum = warpSpectrum * (2 ** (lfos[4] * \warpSpecMD.kr(0, spec: ControlSpec(0, 2))));
    
        freq = \freq.kr(60, spec: ControlSpec(20, 500));
        freq = freq * (2 ** (lfos[5] * \freqMD.kr(0, spec: ControlSpec(0, 3))));

        harmonicRatio = \harmonicRatio.kr(1);
        ampScale = \ampScale.kr(1);
        ampSkew = \ampSkew.kr(0);
        stretch = \stretch.kr(1);

        oddLevel = \oddLevel.kr(1);
        evenLevel = \evenLevel.kr(1);

        partialDriftFreq = \partialDriftFreq.kr(1.5);
        partialDriftMD = \partialDriftMD.kr(0);
        phaseMD = \phaseMD.kr(1);

        /////////////////////////////////////////////////////////////////////////////
    
        chain = initChain.(numPartials, freq);
        chain = makeStretchedHarmonicSeries2.(chain, harmonicRatio, ampScale, ampSkew, stretch);
        chain = evenOddMask.(chain, oddLevel, evenLevel);
        // chain = warpFrequencies.(chain, warpSpectrum);
        chain = generateFormants.(chain, [
            ( \freq: LFNoise1.ar(1).range(400, 600), \bandwidth: 200, \strength: 1.0 ),
            ( \freq: LFNoise1.ar(2).range(1400, 1600), \bandwidth: 100, \strength: 0.8 ),
            ( \freq: LFNoise1.ar(1.5).range(2400, 2600), \bandwidth: 120, \strength: 0.6 )
        ]);
        chain = addLimiter.(chain);
        chain = addSpectralTilt.(chain, \tiltPerOctaveDb.kr(-3, spec: ControlSpec(-3, -12)));
        chain = addCombFilter.(chain, combOffset, combDensity, combSkew, combPeak);

        // chain = addNotchFilter.(chain, 50, 500);
        chain = removeNyquistPartials.(chain);
        
        partialDrift = LFNoise2.ar(partialDriftFreq ! chain[\numPartials]) * partialDriftMD;

        sig = SinOsc.ar(
            freq: chain[\freqs] + partialDrift,
            phase: ({ Rand(0, 2pi) } ! chain[\numPartials]) * phaseMD,
            mul: chain[\amps]
        );

        sig = sig[0,2..].sum + ([-1,1] * sig[1,3..].sum);
    
        sig = sig * -15.dbamp;
    
        sig = sig * \amp.kr(-5, spec: ControlSpec(-5, -25, \lin, 1)).dbamp;
    
        sig = sig * Env.asr(0.001, 1, 0.001).ar(Done.freeSelf, \gate.kr(1));
    
        sig = Limiter.ar(sig);
        sig = LeakDC.ar(sig);
        Out.ar(\out.kr(0), sig);
    }).add;

)
    
(
Routine({

    s.bind {

        Synth(\additiveComb, [

            \freq, 30,
            \freqMD, 0,

            \phaseMD, 1,

            \partialDriftFreq, 1000,
            \partialDriftMD, 100,

            \harmonicRatio, 1,
            \ampSkew, 0,
            \ampScale, 1,
            \stretch, 1,

            \oddLevel, 1,
            \evenLevel, 1,

			\modMF, 0,

			\combOffset, 1,
			\combOffsetMD, 0,

			\combDensity, 0.5,
			\combDensityMD, 1,

			\combSkew, 0.5,
			\combSkewMD, 0,

			\combPeak, 5,
			\combPeakMD, 0,

			\warpSpec, 0.5,
			\warpSpecMD, 0,

			\tiltPerOctaveDb, -4.00,

			\amp, 0,
			\out, 0,
        ]);

    };

}).play;
)

(
    var initChain = { |numPartials, freq|
        (
            freq: freq,
            numPartials: numPartials,
            ratios: (1..numPartials),
            amps: 1 ! numPartials,
        )
    };

    var makeStretchedHarmonicSeries = { |chain, inharmonicity|
        chain[\freqs] = chain[\freq] * chain[\ratios] * (1 + (inharmonicity * chain[\ratios] * chain[\ratios])).sqrt;
        chain;
    };

    var makeStretchedHarmonicSeries2 = { |chain, harmonicRatio, ampScale, ampSkew, stretch|
		//get harmonic integers
        var amps;
		var powN = pow(chain[\ratios], harmonicRatio / 2);
		var relF = (powN * ((1.0 + (powN - 1)) * stretch));
		//harmonic frequency
		chain[\freqs] = (relF * chain[\freq]) ;
		//scale amp and skew
        amps = ((pow(2, ampScale) - ampSkew) * pow(relF, ampSkew));
        chain[\amps] = amps;
    };

    var evenOddMask = {|chain, oddLevel, evenLevel|
        chain[\amps] = chain[\amps].collect { |item, i| if(i.odd){ item * oddLevel; } { item * evenLevel; } };
    };

    var removeNyquistPartials = {|chain|
        var nyquestIdx = chain[\freqs].selectIndices({|item, i| item >= (s.sampleRate * 0.5)});
        chain[\amps].putEach(nyquestIdx, 0);
    };


    
    {
        var chain, sig;
        chain = initChain.(30, 60);
        chain = makeStretchedHarmonicSeries2.(chain, 1, 1, 1, 1);
        removeNyquistPartials.(chain);
        evenOddMask.(chain, 1, 0.5);
        chain[\amps].postln;
    sig = SinOsc.ar(
        freq: chain[\freqs],
        phase: { Rand(0, 2pi) } ! chain[\numPartials],
        mul: chain[\amps]
    );
    }.play


)

arrayedcollection
reduce
s.sampleRate

a = [1,1,1];
a.putEach([0,1,2], 0);



{LFNoise3.ar(1 ! 6).poll(10)}.play;