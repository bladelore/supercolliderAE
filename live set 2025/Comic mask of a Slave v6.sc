
k.gui

(
    ~break = Dictionary();
    ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Missing Sounds 2016/05-KOz_Break_128_PL_1.WAV", ~break, 0.3, \centroid, chans: 2);
    // ~analyzeSlices.("/Users/aelazary/Desktop/Samples etc./Silent Hill/Drum Loops/Silent Hill Homecoming/Witchcraft/efxl_drum_155.wav", ~break, 0.3, \centroid, chans: 2);
)


(
    ~maxGrains = 25;
    ~fftSize = 4096*32;
    ~bufA = nil ?? {Buffer.alloc(s, ~fftSize)};
    ~bufB = nil ?? {Buffer.alloc(s, s.sampleRate * 0.01)};
    ~bufC = nil ?? {Buffer.alloc(s, s.sampleRate * 0.025)};
    ~specBuf = nil ?? {Array.fill(~maxGrains, {Buffer.alloc(s, ~fftSize)})};
    ~specBuf.do{|item| item.zero};
    ~roar_A=Bus.audio(s,2);
    ~roar_B=Bus.audio(s,2);
    ~comp=Bus.audio(s,2);
)

//first slider chance

(

    ~specBuf.do{|item| item.zero};
    ~bufA.zero;
    ~fftSize = 2048*4;
    ~analysisFX = Array.fill(2, {Buffer.alloc(s, ~fftSize)});
    
    t.tempo = 160/60;
 
    ~player = Conductor(\player, t);
    ~player.listen((type: \modality, device: k, key: \tr, button: \fwd));
    ~player.quant_(0);
    ~player.targetSection_(nil);
 
    x = {
        var sample = ~break;

        ~player.label;

            Ndef(\verb).clear;
            Ndef(\verb, {
                var sig = InFeedback.ar(\inbus.ar(0));
                var verb_time = LFNoise2.kr(0.3, 0.1, \time.kr(0.5));
                var verb_damp = LFNoise2.kr(0.2).range(0, \damp.kr(0.7));
                sig = MiVerb.ar(sig, verb_time, \verbMix.kr(1).lag, verb_damp, \hp.kr(0.3).lag, \freeze.kr(0), \diff.kr(0.3).lag , mul: 0.5);
                sig = Rotate2.ar(sig[0], sig[1], SinOsc.ar(0.1));
                sig = sig.tanh;
                sig = Limiter.ar(sig, 0.12, 0.01);
                sig = sig * \gain.kr(0).dbamp;
                
            }).set(
                \inbus, ~miVerb,
                \gain, -10,
                \damp, 1,
                \time, 0.8
            ).play(~bus3);

            // sample -> spectral
            // b = Buffer.read(s, "/Users/aelazary/Projects/soundthread/outfile_2025-11-12_15-42-01.wav");
            // b = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./radioaporee/VandensBokstoTrosas202051.wav");
            // b = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./spannerGuitar/spannerDrumGuitar-2.wav");
            b = Buffer.read(s, "/Users/aelazary/Projects/guitars 2026 Project/guitars 2026-1.wav");
           
            Ndef(\sample).clear;
            Ndef(\sample,
                {
                    var sig;
                    var buf = \buf.kr(0);
                    var pos = \pos.kr(0) * BufFrames.kr(buf);
                    sig = PlayBuf.ar(2, buf, startPos: pos, rate: \rate.kr(0) * BufRateScale.kr(buf), loop: \loop.kr(0), trigger: Impulse.kr(0) + Changed.kr(pos + buf));
                    sig = HPF.ar(sig, 200);
                    sig = sig * \gain.kr(0).dbamp;
                }
            );

            Ndef(\sample).set(\buf, b, \pos, 0, \rate, 0.25, \loop, 1, \gain, -15).play([~spectralGrains.channels].flatten);

            Ndef(\specGrains, \spectralGrains1).set(\inbus, ~spectralGrains, \srcbuf, ~bufA, \specbuf, `[~specBuf], \fftSize, ~fftSize);

            Ndef(\specGrains).set(
                \amp, 1,
                \posRate, 0,
                
                \tFreq, 5,
                \tFreqMR, 0,
                \tFreqMD, 0,

                \spectralFilter, 0.3,

                \num_teeth, 18,
                \comb_phase, 0,
                \comb_phase_mod, 0,
                \comb_width, 0.1,
                
                \windowWidth, 0.9,
                \polarityMod, 1,
                \overlap, 5,

                \companderMD, 3,

                \overdub, 0,
                \midipitch, 0,
                // \feedback, 1,
                \gain, 26,
            );

            Ndef(\specGrains)[999] = \pset -> Pbind(
                \dur, 0.01, 
                \drywet, ~knob.(5),
                \spectralFilter, ~knob.(6).linlin(0, 1, 0.5, 0), 
                \comb_width, ~knob.(7).linlin(0, 1, 0.2, 0.5), 
            );

            Ndef(\specGrains).play(~filter);

            // Ndef(\specGrains).play([~bus2.channels, ~mutant.channels, ~miVerb.channels].flatten);

            Ndef(\filter).clear;
            Ndef(\filter, {
                var sig = InFeedback.ar(\inbus.kr(0), 2);
                var offset = \offset.kr(0.1) * 2pi;
                var lfo = SinOsc.kr((t.tempo / \rate.kr(6)) / SinOsc.ar(t.tempo / \mod.kr(8)), [pi, (pi + offset).wrap(0, 2pi)]).lag;
                // sig = HPF.ar(sig, lfo.linexp(-1,1,50,2000), 2);
                sig = sig * lfo;
            })
                .set(\inbus, ~filter);
            
            Ndef(\filter).play([~bus2.channels, ~miVerb.channels].flatten);

            Ndef(\filter)[999] = \pset -> Pbind(\rate, ~knob.(4).linlin(0,1,0.5,16), \dur, 0.01);

            // Ndef(\lfo).clear;
            // Ndef(\lfo, { SinOsc.kr(1).linexp(-1,1,0,1) });
            // Ndef(\filter).map(\blend, Ndef(\lfo));
        
        ~player.wait;

        ~player.label;

            //mutant
            ~topology=(
                'low' : 0,
                'band' : 1,
                'high' : 2,
                'notch' : 3,
                'peak' : 4,
                'all' : 5,
                'ubp' : 6
            );

            Ndef(\mutantString).fadeTime = 10;
            Ndef(\mutantString).play(~bus1);

            Ndef(\mutantString).set(
                \gain, -6,
                \inbus, ~mutant,
                \midiPitch, 80.midicps(),
                \topology, ~topology[\notch]
            );

            Ndef(\mutantString)[999] = \pset -> Pbind(
                \dur, 0.01,
                \trigRate, ~slider.(0).linexp(0,1,1,100),
                
                \damp, ~slider.(1),
                \exciterFilter, ~slider.(2).linexp(0, 1, 30, 3000),
                \fuzz, ~slider.(3).linlin(0, 1, 0.001, 1) - 0.001,
                \subharmonic, 2,

                \filter, ~slider.(5).linexp(0, 1, 50, 8000),
                \q, ~slider.(6).linexp(0, 1, 0.1, 2),
                \shaper, ~slider.(7),

                \amp, ~knob.(0).linexp(0, 1, 0.01, 0.5).lag(0.01) - 0.01,
                \fb, ~knob.(1).linlin(0,1,-1,1),

                \verbMix, ~knob.(2).linlin(0, 1, -1, 1),
                \verbSize, ~knob.(3).linlin(0,1,1,1000),

                \dispMix, 0,
                \dispResonance, ~knob.(5),
                \dispFreq, 80,
            );

        ~player.wait;

        ~player.label;
            
            Ndef(\mutantString).xset(
                \midiPitch, 20.midicps(),
                \topology, ~topology[\band],
            );
            
            Pdef(\cut1,
                Pbind(
                    \instrument, \segPlayer,
                    // \amp, Pkey(\groupdelta),
                    \amp, 1,
                    \atk, 0.01,
                    \rel, Pkey(\dur) * 1,
                    // \rel, Pkey(\dur) * Pseq([0.5, 0.5, 2, 0.5], inf),
                    \buf, ~break.at(\file),
                    // \rate, Pseq([1, 1, 2, 1], inf),
                    \rate, 1,
                    \oneshot, 1,
                    \gain, 0,
                    \sliceStart, 0,
                    // \stutterPat, Pseq([2, 3], inf),
                    \stutterPat, Pstep(Pseq([4, 1], inf), Pseq([8, 1], inf), inf),
                    \stutterRange, Pstep(Pseq([5, 1], inf), 4, inf),

                    // \stutterPat, 4,
                    // \stutterRange, 5,

                    \slice, ~pGetSlice.((Pseries(0, Pkey(\stutterRange), inf) + Pkey(\sliceStart)), ~break).stutter(Pkey(\stutterPat)),
                    \pitchMix, 0.8,
                    // \pitchRatio, 1,
                    \windowSize, ~pmodenv.(Pseq([0.01, 0.04], inf), 2),
                    \pitchDispersion, 0.01,
                    \timeDispersion, 0.5,
                    \out, [~roar_A, ~mutant]
                )
            );

            Pdef(\p1,
                ~makeSubdivision.(
                    PlaceAll([1, 1, 1, 1], inf),
                    PlaceAll([4, 4, 4, 4], inf)
                )
            );

            Pdef(\p2,
                ~makeSubdivision.(
                    PlaceAll([2, 2, 2, 2], inf),
                    PlaceAll([4, 4, 4, 4], inf)
                )
            );

            Pdef(\chance_mod,
                    Pbind(\dur, Pfunc({|ev|
                        var val;
                        var chance = ev[\chance].coin;
                        if(chance == true) {val = ev[\dur];}{ val = Rest(ev[\dur])};
                        val;
                    })) <>
                Pbind(\chance, ~slider.(0))  
            );


           Pdef(\perc,
                Pdef(\chance_mod) <>
                Pdef(\cut1) <>
                ~filterBeat.(key: Pkey(\cyclecount), beat:[2, 5], mod: 9, reject: 0) <>
                Pdef(\p1)
            ).play(t);

            Ndef(\roar_A).clear;
            Ndef(\roar_A, \roar)
            .set(
                \inbus, ~roar_A,
                \drive, -9.0,
                \toneFreq, 6600.0,
                \toneComp, 1,
                \drywet, 1,
                \bias, 0,
                \filterFreq, 3000,
                \filterBP, 0,
                \filterRes, 0.3,
                \filterBW, 0.5,
                \filterPre, 2.0,
                \feedAmt, 9.0,
                \feedFreq, 500.0,
                \feedBW, 0.1,
                \feedDelay, 0.1,
                \feedGate, 1,
                \gain, 6.0,
                \amp, 1.0,
            );

            Ndef(\lfo1, { SinOsc.ar(t.tempo / 4, 0).linlin(-1, 1, -0.99, 0.75) });
            Ndef(\lfo2, { SinOsc.ar(t.tempo / 6, 0).linlin(-1, 1, 0, 1) });

            Ndef(\roar_A).map(
                \tone, Ndef(\lfo1),
                \filterLoHi, Ndef(\lfo2)
            );

            Ndef(\roar_A).play([~bus2.channels, ~miVerb.channels].flatten);

        ~player.wait;

        ~player.label;

            Ndef(\roar_A, \roar)
            .set(
                \inbus, ~roar_A,
                \drive, -9.0,
                \toneFreq, 6600.0,
                \toneComp, 1,
                \drywet, 1,
                \bias, 0,
                \filterFreq, 40,
                \filterBP, 0,
                \filterRes, 0.3,
                \filterBW, 0.5,
                \filterPre, 2.0,
                \feedAmt, 9.0,
                \feedFreq, 500.0,
                \feedBW, 0.1,
                \feedDelay, 0.1,
                \feedGate, 1,
                \gain, 6.0,
                \amp, 1.0,
            );

        ~player.wait;

        ~player.label;

            Pdef(\cut1,
                Pbind(
                    \instrument, \segPlayer,
                    // \amp, Pkey(\groupdelta),
                    \amp, 1,
                    \atk, 0.01,
                    \rel, Pkey(\dur) * 1,
                    // \rel, Pkey(\dur) * Pseq([0.5, 0.5, 2, 0.5], inf),
                    \buf, ~break.at(\file),
                    // \rate, Pseq([1, 1, 2, 1], inf),
                    \rate, 1,
                    \oneshot, 1,
                    \gain, -6,
                    \sliceStart, 0,
                    \stutterPat, 1,
                    // \stutterRange, Pstep(Pseq([5], inf), 16, inf),
                    \slice, ~pGetSlice.((Pseries(5, 9, inf) + Pkey(\sliceStart)), ~break).stutter(Pkey(\stutterPat)),
                    \pitchMix, 0.8,
                    // \pitchRatio, 1,
                    \windowSize, ~pmodenv.(Pseq([0.01, 0.04], inf), 2),
                    \pitchDispersion, 0.01,
                    \timeDispersion, 0.5,
                    \out, [~roar_A, ~mutant, ~resonator]
                )
            );

        ~player.wait;

        ~player.label;

            Pdef(\fmString,
                Pbind(
                    \instrument, \fmString,
                    \midiPitch, Pseq([79].midicps(), inf),
                    \atk, 0,
                    \rel, 1,
                    // \fb, ~pmodenv.(Pwhite(-1, 1, inf), 0.5, inf, \sine),
                    \fb, 1,
                    \filter, 1000,
                    // \fuzz, ~pmodenv.(Pseq([0, 1, 1],inf), 4, inf, \sine),
                    \fuzz, 0.5,
                    \subharmonic, Pseq([3, 2.8], inf),
                    \exciterFilter, 3000,
                    // \exciterAttack, 3000,
                    \exciterRelease, Pseq([3000, 2000] * 1, inf),

                    \gain, 0,
                    \amp, ~pmodenv.(Pseq([0.1, 0.05],inf), 4, inf, \sine),
                    \out, ~roar_A,
                    
                    // \dur, 16
                )
                
                <> ~filterBeat.(key: Pkey(\cyclecount), beat:[4], mod: 9, reject: 0)
                // <> ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 2, 5], mod: 9, reject: 1)
                <> Pdef(\p1)
            ).play(t, quant: 1);

        ~player.wait;

        ~player.label(\time);

            // Ndef(\sample).set(\rate, 0.5);
            // Ndef(\filter).set(\rate, 1);

            Pdef(\reso, (
                Pbind(\out, ~bus2, \atk, 0.01, \dec, 0.5, \distort, 0.7, \freq, 40, \inGain, -25, \gain, -12) <> Pdef(\resonator)
            )).play;

            Pdef(\cut1,
                Pbind(
                    \instrument, \segPlayer,
                    // \amp, Pkey(\groupdelta),
                    \amp, 1,
                    \atk, 0.01,
                    \rel, Pkey(\dur) * 1,
                    // \rel, Pkey(\dur) * Pseq([0.5, 0.5, 2, 0.5], inf),
                    \buf, sample.at(\file),
                    // \rate, Pseq([1, 1, 2, 1], inf),
                    \rate, 1,
                    \oneshot, 1,
                    \gain, -6,
                    \sliceStart, 0,
                    \stutterPat, 1,
                    // \stutterRange, Pstep(Pseq([5], inf), 16, inf),
                    \slice, ~pGetSlice.((Pseries(6, 59, inf) + Pkey(\sliceStart)), sample).stutter(Pkey(\stutterPat)),
                    \pitchMix, 0.8,
                    // \pitchRatio, 1,
                    \windowSize, ~pmodenv.(Pseq([0.01, 0.04], inf), 2),
                    \pitchDispersion, 0.01,
                    \timeDispersion, 0.01,
                    \out, [~bus2, ~mutant, ~resonator]
                )
            );

            Pdef(\perc,
                Pdef(\cut1) <>
                ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 3, 5, 6], mod: 6, reject: 0) <>
                Pdef(\p1)
            ).play(t);

        ~player.wait;

        ~player.label(\marker);

            Pdef(\reso).stop;

            Pdef(\cut1,
                Pbind(
                    \instrument, \segPlayer,
                    // \amp, Pkey(\groupdelta),
                    \amp, Pwhite(0.8, 1, inf),
                    \atk, Prand([0.1, 0.02], inf),
                    \rel, Pkey(\dur) * 0.5,
                    // \rel, Pkey(\dur) * Pseq([0.5, 0.5, 2, 0.5], inf),
                    \buf, sample.at(\file),
                    // \rate, Pseq([1, 1, 2, 1], inf),
                    \rate, 1,
                    \oneshot, 1,
                    \gain, -6,
                    \sliceStart, Pstep(Pseq([0, 5, 0, 10], inf), 4, inf),
                    \stutterPat, 1,
                    // \stutterRange, Pstep(Pseq([5], inf), 16, inf),
                    \slice, ~pGetSlice.((Pseries(0, 1, inf).wrap(0, 5) + Pkey(\sliceStart)), sample).stutter(Pkey(\stutterPat)),
                    \pitchMix, 0.8,
                    // \pitchRatio, 1,
                    \windowSize, ~pmodenv.(Pseq([0.01, 0.04], inf), 2),
                    \pitchDispersion, 0.01,
                    \timeDispersion, 0.01,
                    \out, [~bus2, ~mutant, ~resonator]
                )
            );

            // Pdef(\fmKick,
            //     Pbind(
            //         \instrument, \fmKick,
            //         // \dur, 11,
            //         \dur, Pkey(\dur) * 4,
            //         \freq, 65,
            //         \atk, 0.2,
            //         \dec, 1,
            //         \index, 0.1,
            //         \fb, 1,
            //         \drive, 10,
            //         \spread, 0,
            //         \sweep, 16,
            //         \gain, 0,
            //         \out, ~bus2  
            //     )
            //     <>
            //     ~filterBeat.(key: Pkey(\groupcount), beat: [1]) <>
            //     ~filterBeat.(key: Pkey(\cyclecount), beat:[1], mod: 9, reject: 0) <>
            //     Pdef(\p1)
            // ).play(t);

            Pdef(\subParams,
                Pbind(
                    \instrument, \eightoheight1,
                    \amp, 1,
                    // \dur, 0.125,
                    \dur, Pkey(\dur) * 4,
                    \globalLag, 9,
                    \freq, Pseq(([25, 25, 25, 25] + 2).midicps, inf),
                    \sweep, 5,
                    \drive, 15,
                    \driveMix, 1,
                    // \drive, Pseq([20, 10] * 0.1, inf),
                    // \atk, Pseq([1, 2], inf),
                    \atk, 2,
                    \dec, 0.1,
                    \sus, 0.5,
                    \rel, 1,
                    \gain, 15,
                    \out, ~bus4
                )
                <>
                ~filterBeat.(key: Pkey(\groupcount), beat: [1]) <>
                ~filterBeat.(key: Pkey(\cyclecount), beat:[1], mod: 9, reject: 0) <>
                Pdef(\p1)
            ).play(t);

        ~player.wait;

        ~player.label;

            Pdef(\cut1,
                Pbind(
                    \instrument, \segPlayer,
                    // \amp, Pkey(\groupdelta),
                    \amp, Pwhite(0.8, 1, inf),
                    \atk, Prand([0.1, 0.02], inf),
                    \rel, Pkey(\dur) * 0.5,
                    // \rel, Pkey(\dur) * Pseq([0.5, 0.5, 2, 0.5], inf),
                    \buf, sample.at(\file),
                    // \rate, Pseq([1, 1, 2, 1], inf),
                    \rate, 1,
                    \oneshot, 1,
                    \gain, -6,
                    \sliceStart, Pstep(Pseq([0, 5, 0, 10], inf), 4, inf),
                    \stutterPat, 1,
                    // \stutterRange, Pstep(Pseq([5], inf), 16, inf),
                    \slice, ~pGetSlice.((Pseries(0, 1, inf).wrap(0, 5) + Pkey(\sliceStart)), sample).stutter(Pkey(\stutterPat)),
                    \pitchMix, 0.8,
                    // \pitchRatio, 1,
                    \windowSize, ~pmodenv.(Pseq([0.01, 0.04], inf), 2),
                    \pitchDispersion, 0.01,
                    \timeDispersion, 0.05,
                    \out, [~bus2, ~mutant, ~resonator]
                )
            );

            Pdef(\perc,
                Pdef(\cut1) <>
                ~filterBeat.(key: Pkey(\cyclecount), beat:[3, 5], mod: 5, reject: 1) <>
                Pdef(\p1)
            ).play(t);

            // Pdef(\fmKick,
            //     Pbind(
            //         \instrument, \fmKick,
            //         // \dur, 11,
            //         \dur, Pkey(\dur) * 4,
            //         \freq, 65,
            //         \atk, 0.2,
            //         \dec, 1,
            //         \index, 0.1,
            //         \fb, 1,
            //         \drive, 10,
            //         \spread, 0,
            //         \sweep, 16,
            //         \gain, 0,
            //         \out, ~bus2  
            //     )
            //     <>
            //     ~filterBeat.(key: Pkey(\groupcount), beat: [1]) <>
            //     ~filterBeat.(key: Pkey(\cyclecount), beat:[1], mod: 9, reject: 0) <>
            //     Pdef(\p1)
            // ).play(t, quant: 1);

        ~player.wait;

        ~player.label;

            Pdef(\cut1,
                Pbind(
                    \instrument, \segPlayer,
                    // \amp, Pkey(\groupdelta),
                    \amp, 1,
                    \atk, 0.01,
                    \rel, Pkey(\dur) * 1,
                    // \rel, Pkey(\dur) * Pseq([0.5, 0.5, 2, 0.5], inf),
                    \buf, sample.at(\file),
                    // \rate, Pseq([1, 1, 2, 1], inf),
                    \rate, 1,
                    \oneshot, 1,
                    \gain, -6,
                    \sliceStart, 0,
                    \stutterPat, 1,
                    // \stutterRange, Pstep(Pseq([5], inf), 16, inf),
                    \slice, ~pGetSlice.((Pseries(6, 59, inf) + Pkey(\sliceStart)), sample).stutter(Pkey(\stutterPat)),
                    \pitchMix, 0.8,
                    // \pitchRatio, 1,
                    \windowSize, ~pmodenv.(Pseq([0.01, 0.04], inf), 2),
                    \pitchDispersion, 0.01,
                    \timeDispersion, 0.01,
                    \out, [~roar, ~mutant, ~resonator]
                )
            );

        ~player.wait;

        ~player.label;

            Pdef(\perc).stop;
            Pdef(\fmString).stop;
            // Ndef(\sample).play(~bus1);
            // Ndef(\sample2).play(~bus1);

        ~player.wait;

        ~player.label(\beat2);

            Ndef(\sample).set(\rate, 1, \pos, 0.9,);

            Pdef(\reso).play;

            Ndef(\roar_A, \roar)
            .set(
                \inbus, ~roar_A,
                \drive, 0.0,
                \toneFreq, 6600.0,
                \toneComp, 0,
                \drywet, 1,
                \bias, 0,
                \filterFreq, 40,
                \filterBP, 0,
                \filterRes, 0.3,
                \filterBW, 0.5,
                \filterPre, 2.0,
                \feedAmt, 5.0,
                \feedFreq, 500.0,
                \feedBW, 0.1,
                \feedDelay, 0.1,
                \feedGate, 1,
                \gain, 6.0,
                \amp, 1.0,
            );

            Pdef(\cut1,
                Pbind(
                    \instrument, \segPlayer,
                    \amp, 1,
                    \atk, 0.01,
                    \rel, Pkey(\dur) * 1,
                    \buf, sample.at(\file),
                    \rate, 1,
                    \oneshot, 1,
                    \gain, -6,
                    \sliceStart, 0,
                    \stutterPat, 1,
                    \stutterRange, Pstep(Pseq([5, 1], inf), 4, inf),

                    \slice, ~pGetSlice.((Pseries(0, Pkey(\stutterRange), inf) + Pkey(\sliceStart)), sample).stutter(Pkey(\stutterPat)),
                    \pitchMix, 0.5,
                    \windowSize, ~pmodenv.(Pseq([0.01, 0.04], inf), 2),
                    \pitchDispersion, 0.01,
                    \timeDispersion, 0.5,
                    \out, [~roar_A, ~mutant, ~resonator]
                )
            );

            Pdef(\perc,
                Pdef(\cut1) <>
                Pdef(\p2)
            ).play(t);

        ~player.wait;

        ~player.label;

            \end.postln; 
            
            Ndef(\sample).stop(fadeTime: 10);
            Pdef(\perc).stop;
            Pdef(\subParams).stop;

            Ndef(\mutantString).set(
                \inbus, ~mutant,
                \midiPitch, 80.midicps(),
                \topology, ~topology[\low]
            );

            Ndef(\sample).play(~mutant);
            Ndef(\sample2).stop(fadeTime: 5);

            Ndef(\mutantString)[999] = \pset -> Pbind(
                \dur, 0.01,
                \trigRate, ~slider.(0).linexp(0,1,1,100),
                
                \damp, ~slider.(1),
                \exciterFilter, ~slider.(2).linexp(0, 1, 30, 3000),
                \fuzz, ~slider.(3).linlin(0, 1, 0.001, 1) - 0.001,
                // \subharmonic, ~slider.(4).linlin(0, 1, 1, 4),
                \subharmonic, 2,

                \filter, ~slider.(5).linexp(0, 1, 50, 8000),
                \q, ~slider.(6).linexp(0, 1, 0.1, 2),
                \shaper, ~slider.(7),

                \amp, ~knob.(0).lag(0.01),
                \fb, ~knob.(1).linlin(0,1,-1,1),

                \verbMix, ~knob.(2).linlin(0, 1, -1, 1),
                \verbSize, ~knob.(3).linlin(0,1,1,1000),

                \dispMix, ~knob.(4),
                \dispResonance, ~knob.(5),
                \dispFreq, 80,
            );

        ~player.wait;

        ~player.label;

            Ndef(\specGrains).stop;

    }.fork(t);
);

(
    Ndef(\specGrains).stop;
    Ndef(\mutantString).stop(fadeTime: 10);
)

// (
//     d = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./tom erbe impulse responses/e616ImpResp.wav");
//     Pmono(\convolved_pulsar_mono,
//         \buf, d,
//         \window, 4096,
//         \triggerRate, 14,
//         \fluxMF, 1.5,
//         \fluxMD, 1,
//         \grainFreq, 41,
//         \overlap, 1,
//         \pmRatio, 40,
//         \pmIndex, 0.2,
//         \density, 1,
//         \polarityMod, 1,
//         \gain, -18,
//         \out, ~bus1
//     ).play(t);
// )