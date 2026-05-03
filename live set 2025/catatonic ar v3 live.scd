(

    ~getBuffers = {|file, frames|
        SoundFile.openRead(file).numChannels.collect({|i| Buffer.readChannel(s, file, channels:[i], numFrames: frames);});
    };

    // file = "/Users/aelazary/Desktop/Samples etc./tom erbe impulse responses/effectronImpResp.wav";
    // file = "/Users/aelazary/Desktop/Samples etc./scythes/POV freehand peening a scythe blade using a bar peen anvil (to really annoy the neighbours).wav";
    // file = "/Users/aelazary/Desktop/Samples etc./scythes/Sharpen the scythe.wav";
    // file = "/Users/aelazary/Desktop/Samples etc./matchstick burning/match impulse.wav";

    ~break = Dictionary(); ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./radioaporee/020420241142PMwindrecambimicsairharp.wav", ~break, 0.31, \centroid, chans: 2);
    
    // spectral buffer
    ~specBuff = SpecBuf(s, "/Users/aelazary/Desktop/Samples etc./guitar samples/electric 25-3-4.wav", 16384*2, overlaps: 2, numChannels: 2);

    // interp player
    ~breakA = Dictionary(); ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./contact mics/guitar chain.wav", ~breakA, 0.31, \crest, chans: 2);
    ~breakB = Dictionary(); ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./contact mics/guitar chain.wav", ~breakB, 0.31, \crest, chans: 2);

    // wavetables
    ~table1 = ~getBuffers.("/Users/aelazary/Desktop/Samples etc./scythes/POV freehand peening a scythe blade using a bar peen anvil (to really annoy the neighbours).wav", -1);
    ~table2 = ~getBuffers.("/Users/aelazary/Desktop/Samples etc./guitar samples/electric 25-3-4.wav", -1);

)

~table1

(    
    var player;
    t.tempo = 160/60;
    //player
    player = Conductor(\player, t);
    player.quant_(0);
    player.targetSection_(nil);
    // player.targetSection_(\marker);
    player.listen((type: \modality, device: k, key: \tr, button: \fwd));

    x = {

        player.label;

            Pdef(\table,
                PmonoArtic(
                    \fbWt_single,
                    \buffer, [~table1],
                    // \buffer, Pstep(Pseq([[~table1], [~table2]], inf), 2, inf),
                    \amp, 1,
                    \fb, 0,
                    \stereoOffset, 0,
                    \legato, Prand([0.5, 1], inf),
                    \globalLag, 16,
                    \freq, Pstep(Pseq([10, 15], inf), 8, inf),
                    \sweep, 16,

                    \size, 2048,

                    \dur, 2,

                    \atk, 1,
                    \dec, 0.8,
                    \sus, 1,
                    \rel, 2,

                    \brf, PmodEnv(Pexprand(50, 10000, inf), 3, \sine, inf),
                    \lpf, ~slider.(0).linexp(0,1, 50, 20000),

                    \gain, 30,
                    \out, [~bus2, ~convolve_A]
                )
            ).play(t);
             
            // b ?? {b = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./Field recs/drain paris audrey.wav")};
            b = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./Field recs/drain paris audrey.wav");
            c = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./radioaporee/20200720JardindumuseeduLouvreLensPCMM1002HarpeseloliennesDidierFermentBrunoTondelilier.wav");
            d = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./vocal samples/crackles2.wav");

            Ndef(\sample).clear;
            Ndef(\sample,
                {
                    var sig;
                    var buf = \buf.kr(0);
                    var pos = \pos.kr(0) * BufFrames.kr(buf);
                    sig = PlayBuf.ar(2, buf, startPos: pos, rate: \rate.kr(0) * BufRateScale.kr(buf), loop: \loop.kr(0), trigger: Impulse.kr(0) + Changed.kr(pos + buf));
                    sig = sig * \gain.kr(0).dbamp;
                });
                

            Ndef(\morph, \cepstralMorph_fx).set(\inbus_A, ~convolve_A, \inbus_B, ~convolve_B);
            Ndef(\morph)[999] = \pset -> Pbind(
                \amp, 1,
                \dur, 0.01,
                \gain, 0,
                \atk, 10,
                \rel, 100,
                \swap, ~slider.(7),
                \drywet, 1,
            );
            Ndef(\morph).play(~bus1);
            
            Ndef(\sample).fadeTime = 5;
            Ndef(\sample).set(\buf, c, \pos, 0, \rate, 1, \loop, 1, \gain, -20).play(~convolve_B);

        player.wait;

        player.label;

            Pdef(\table,
                PmonoArtic(
                    \fbWt_single,
                    \buffer, [~table1],
                    \amp, 1,
                    \fb, 0,
                    // \stereoOffset, PmodEnv(Pexprand(0.01, 1, inf), 1, \sine, inf),
                    \stereoOffset, 0.1,
                    \legato, Prand([0.5, 1], inf), 
                    \globalLag, 16,
                    \freq, 30,
                    \sweep, 16,

                    \size, \512,

                    \dur, Prand([2, Rest(2)], inf),

                    \atk, 1,
                    \dec, 0.8,
                    \sus, 1,
                    \rel, 2,

                    \brf, PmodEnv(Pexprand(50, 10000, inf), 3, \sine, inf),
                    \lpf, ~slider.(0).linexp(0,1, 50, 20000),

                    \gain, 10,
                    \out, [~bus2, ~convolve_A]
                )
            ).play(t);

        player.wait;

        player.label;

            Ndef(\sample).stop;
                    
            // Pdef(\table).stop;

            Pdef(\interp,
                Pbind(
                    \instrument, \segInterp,
                    // \amp, Pkey(\groupdelta),
                    \dur, Pwrand([1, 0.25, 0.5, 2], [1, 2, 3, 4].normalizeSum, inf),
                    // \dur, 0.25,
                    \amp, 1,
                    \atk, 1,
                    \dec, 1,
                    \sus, 0,
                    \rel, 1,
                    \windowSize, Prand([1024, 4096, 8192], inf),
                    // \rel, Pkey(\dur) * Pseq([0.5, 0.5, 2, 0.5], inf),
                    \bufA, ~breakA.at(\file),
                    \bufB, ~breakB.at(\file),
                    // \rate, Pseq([1, 1, 2, 1], inf),
                    \rate, Prand([1, 2], inf),
                    \oneshot, 1,
                    \gain, 12,
                    \pan, Pwhite(-0.6, 0.6, inf),
                    \sliceIdx, Pseries(0, 1, inf).wrap(0, 16),
                    // \sliceIdx, 0,
                    \sliceA, ~pGetSlice.(Pkey(\sliceIdx), ~breakA),
                    \sliceB, ~pGetSlice.(Pkey(\sliceIdx) + 1, ~breakB),
                    \out, ~bus4,
                    // \interp, 0.5,
                )
            ).play(t);

        player.wait;

        player.label;

            Pdef(\interp).stop;

        player.wait;

        player.label;

            // Pdef(\table).stop;
            Ndef(\sample).stop;
            Ndef(\sample).fadeTime = 10;
            Ndef(\sample).set(\buf, c, \pos, 0.8, \rate, 1, \loop, 0, \gain, -40).play(~bus1);

        player.wait;
        
        player.label;

            Pdef(\table,
                PmonoArtic(
                    \fbWt_single,
                    \buffer, [~table1],
                    \amp, 1,
                    \fb, 0,
                    \stereoOffset, 1,
                    \legato, Prand([0.5, 1], inf),
                    \globalLag, 16,
                    \freq, Pseq(([30, 31, 30, 30]).midicps + 1, inf),
                    \sweep, 16,

                    \size, \512,

                    \dur, Prand([2, Rest(2)], inf),

                    \atk, 1,
                    \dec, 0.8,
                    \sus, 1,
                    \rel, 2,

                    \brf, PmodEnv(Pexprand(50, 10000, inf), 3, \sine, inf),
                    \lpf, ~slider.(0).linexp(0,1, 50, 20000),

                    \gain, 10,
                    \out, [~bus2, ~convolve_A]
                )
            ).play(t);

        player.wait;

        player.label;

            Ndef(\sample).set(\buf, b, \pos, 0, \rate, 1, \loop, 1, \gain, -20).play(~convolve_B);

            Pdef(\table,
                PmonoArtic(
                    \fbWt_single,
                    \buffer, [~table1],
                    \amp, 1,
                    \fb, 0,
                    \stereoOffset, 1,
                    \legato, Prand([0.5, 1], inf),
                    \globalLag, 16,
                    \freq, Pseq(([30, 31, 30, 30]).midicps + 1, inf),
                    \sweep, 16,

                    \size, Prand([1024, 4096, 2048], inf ),

                    \dur, Prand([2, Rest(2)], inf),

                    \atk, 1,
                    \dec, 0.8,
                    \sus, 1,
                    \rel, 2,

                    \brf, PmodEnv(Pexprand(50, 10000, inf), 3, \sine, inf),
                    \lpf, ~slider.(0).linexp(0,1, 50, 20000),

                    \gain, 10,
                    \out, [~bus2, ~convolve_A]
                )
            ).play(t);

        player.wait;

        player.label;

            Pdef(\table,
                PmonoArtic(
                    \fbWt_single,
                    \buffer, [~table1],
                    \amp, 1,
                    \fb, 0,
                    \stereoOffset, 1,
                    \legato, Prand([0.5, 1], inf),
                    \globalLag, 16,
                    \freq, Pseq(([30, 31, 30, 30]).midicps + 1, inf),
                    \sweep, 16,

                    \size, 512,

                    \dur, Prand([2, Rest(2)], inf),

                    \atk, 1,
                    \dec, 0.8,
                    \sus, 1,
                    \rel, 2,

                    \brf, PmodEnv(Pexprand(50, 10000, inf), 3, \sine, inf),
                    \lpf, ~slider.(0).linexp(0,1, 50, 20000),

                    \gain, 10,
                    \out, [~bus2, ~convolve_A]
                )
            ).play(t);

        player.wait;

        player.label;

            Pdef(\specSample,
                Pmono(\fftStretch_magFilter_stereo,
                    \dur, 0.01,
                    \amp, 1,
                    \gain, -10,
                    \buf, [~specBuff.file], \analysis, [~specBuff.analysisBufs], \fftSize, [~specBuff.fftSize],
                    \pitchRatio, 1,
                    \len, ~knob.(4),
                    \rate, ~knob.(5) * 4,
                    \pos, ~knob.(6),
                    \remove, ~knob.(7).linexp(0,1,0.01, 1) - 0.01,
                    \pan, ~pmodenv.(Pwhite(-0.6, 0.6), 3, inf, \sin),
                    \thresh, 50,
                    \amp, PmodEnv(Pseq([0.1, 2, 0.1], 1), 3, [0.5, -0.5]).loop,
                    \out, [~convolve_A]
                )
            ).play(t, quant: 1);

        player.wait;

        player.label;
        
            Pdef(\table,
                PmonoArtic(
                    \fbWt_single,
                    \buffer, Pstep(Pseq([[~table1], [~table2]], inf), 2, inf),
                    \amp, 1,
                    \fb, 0,
                    \stereoOffset, 1,
                    \legato, Prand([0.5, 1], inf),
                    \globalLag, 16,
                    \freq, Pseq(([30, 31, 30, 30]).midicps + 1, inf),
                    \sweep, 16,
                    \size, 512,
                    \cycleOffset, 0.5,
                    \dur, 2,
                    // \dur, 2,
                    \atk, 1,
                    \dec, 0.8,
                    \sus, 1,
                    \rel, 2,

                    \brf, PmodEnv(Pexprand(50, 10000, inf), 3, \sine, inf),
                    \lpf, ~slider.(0).linexp(0,1, 50, 20000),

                    \gain, 10,
                    \out, [~bus2]
                )
            ).play(t);

        player.wait;

        player.label;

            ~table1 = ~getBuffers.("/Users/aelazary/Desktop/Samples etc./matchstick burning/match impulse.wav", -1);

            Pdef(\table,
                PmonoArtic(
                    \fbWt_single,
                    \buffer, [~table1],
                    \amp, 1,
                    \fb, 0,
                    \stereoOffset, 1,
                    \legato, Prand([0.5, 1], inf),
                    \globalLag, 16,
                    \freq, Pseq(([30, 62, 30, 30]).midicps + 1, inf),
                    \sweep, 16,
                    \size, Prand([1024, 4096, 2048], inf ),
                    \dur, 0.5,


                    \filterMix, 1,

                    \dur, 0.25,
                    \atk, 0.1,
                    \dec, 0.2,
                    \sus, 0,
                    \rel, 0,

                    \gain, 10,

                                        \brf, PmodEnv(Pexprand(50, 10000, inf), 3, \sine, inf),
                    \lpf, ~slider.(0).linexp(0,1, 50, 20000),
                    \out, [~bus2]
                )
            ).play(t);

        player.wait;

        player.label(\marker);

            ~table1 = ~getBuffers.("/Users/aelazary/Desktop/Samples etc./matchstick burning/match impulse.wav", 4096);

            Pdef(\table,
                PmonoArtic(
                    \fbWt_single,
                    \buffer, Pstep(Pseq([[~table1], [~table2]], inf), 2, inf),
                    // \buffer, [~table1],
                    \amp, 1,
                    \fb, 0,
                    \stereoOffset, 0.1,
                    \legato, Prand([0.5, 1], inf),
                    \globalLag, 16,
                    \freq, Pseq(([30, 62, 30, 30]).midicps, inf),
                    \sweep, 16,
                    // \size, 512,
                    \size, Prand([1024, 4096, 2048], inf ),
                    \dur, 0.5,
                    

                    \filterMix, 0,

                    \dur, 0.25,
                    \atk, 0.1,
                    \dec, 0.2,
                    \sus, 0,
                    \rel, 0.1,

                    \brf, PmodEnv(Pexprand(50, 10000, inf), 3, \sine, inf),
                    \lpf, ~slider.(0).linexp(0,1, 50, 20000),

                    \gain, 10,
                    \out, [~bus2]
                )
            ).play(t);

        player.wait;

        player.label;

            Pdef(\table,
                PmonoArtic(
                    \fbWt_single,

                    \buffer, [~table1],
                    \amp, 1,
                    \fb, 0,
                    \stereoOffset, 0.1,
                    \legato, Prand([0.5, 1], inf),
                    \globalLag, 16,
                    \freq, Pseq(([30, 62, 30, 30] - 2).midicps, inf),
                    \sweep, 16,

                    \size, Prand([1024, 4096, 2048], inf ),
                    \dur, 0.5,

                    \filterMix, 1,

                    \dur, 0.25,
                    \atk, 0,
                    \dec, 0.2,
                    \sus, 0,
                    \rel, 0.1,

                    \brf, PmodEnv(Pexprand(50, 10000, inf), 3, \sine, inf),
                    \lpf, ~slider.(0).linexp(0,1, 50, 20000),

                    \gain, 0,
                    \out, [~bus2]
                )
            ).play(t);

            // Pdef(\kick,
            //     Pbind(
            //         \dur, Pseq([2, Rest(1), 1.5, Rest(0.5), 2], inf),
            //         \dec, Pkey(\dur),
            //         \gain, 30,
            //         \freq, 47,
            //         \instrument, \eightoheight1,
            //         \out, ~bus3,
            //     )   
            // ).play(t);

            // Pdef(\clap,
            //     Pbind(
            //         \dur, Pseq([Rest(2), 2], inf),
            //         \gain, 20,
            //         \instrument, \clap,
            //         \out, ~bus3,
            //     )   
            // ).play(t);

        player.wait;

        player.label;

            Pdef(\table).stop;

            Pdef(\kick).stop; Pdef(\clap).stop;

        player.wait;

        player.label;

            Pdef(\specSample).stop;
            
    }.fork;
)



Ndef(\morph).stop(fadeTime: 10);