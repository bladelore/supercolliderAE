//spectral grains guitar
(
    ~maxGrains = 25;
    ~fftSize = 4096*32;
    ~bufA = Buffer.alloc(s, ~fftSize);
    ~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
    ~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
    ~specBuf = Array.fill(~maxGrains, {Buffer.alloc(s, ~fftSize)});
    ~specBuf.do{|item| item.zero};
)

{SoundIn.ar(\in.kr(0)!2);}.play

(

b = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./feedback cymbals/feedback cymbals-4.wav");
c = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./feedback cymbals/feedback cymbals-5.wav");

~new_advance.();

x = {
    \a.postln;

        ~bufA.zero;
        ~specBuf.do{|item| item.zero};
        //
        Ndef(\guitar, \input).set(\in, 0, \amp, 1, \gain, -12).play(~spectralGrains);

        //
        Ndef(\sample).clear;
        Ndef(\sample,
            {
                var sig;
                var buf = \buf.kr(0);
                var pos = \pos.kr(0) * BufFrames.kr(buf);
                sig = PlayBuf.ar(2, buf, startPos: pos, rate: \rate.kr(0) * BufRateScale.kr(buf), loop: \loop.kr(0), trigger: Impulse.kr(0) + Changed.kr(pos + buf));
                sig = sig * \gain.kr(0).dbamp;
            });
        Ndef(\sample).fadeTime = 10;
        Ndef(\sample).set(\buf, b, \pos, 0, \rate, 0.5, \loop, 1, \gain, -20).play(~convolve_A);

        //
        Ndef(\specGrains, \spectralGrains1)
            .set(\inbus, ~spectralGrains, \srcbuf, ~bufA, \specbuf, `[~specBuf], \fftSize, ~fftSize);

        Ndef(\specGrains)[999] = \pset -> Pbind(
            \amp, ~slider.(0),
            \dur, 0.01,
            \posRate, ~knob.(0).linlin(0,1,0.01,1),
            \tFreq, ~knob.(1).clip(0.001,1).linexp(0.001,1,10,1000),
            \spectralFilter, 1 - ~knob.(2),

            \num_teeth, ~knob.(3).linlin(0,1,1,32),
            \comb_phase, ~knob.(4),
            \comb_width, ~knob.(5),
            
            \windowWidth, 0.9,
            \polarityMod, 1,
            \overlap, 10,

            \companderMD, ~knob.(6).linexp(0,1,0.01,20),

            \overdub, ~knob.(7),
            \midipitch, -12,

            \gain, 24,
        );
        Ndef(\specGrains).play(~bus1);

        //
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

    ~advance.wait;

        \b.postln;

        Ndef(\guitar, \input)
            .set(\in, 0, \amp, 1, \gain, -12)
            .play(~spectralGrains);

        Ndef(\specGrains).play(~bus1);

    ~advance.wait;

        c = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./wand recs/speaker2.wav");

        Ndef(\sample)
            .set(\buf, c, \rate, 1)
            .play(~bus2);

    ~advance.wait;

        \d.postln;

        Ndef(\guitar, \input)
            .set(\in, 0, \amp, 1, \gain, -12)
            .play(~spectralGrains);

        d = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./wand recs/speaker1.wav");

        Ndef(\sample).set(\buf, d).play(~bus3); 

        Ndef(\morph).stop;
        Ndef(\specGrains).play(~bus1);
            
}.fork(t)
)

Ndef(\specGrains).stop(fadeTime: 10)

(
    Ndef(\sample).stop(fadeTime: 30);
)

(
SynthDef(\gutter_tracker, {
    var sig, freq1, freq2, pitch;
    var mod, omega, damp, rate, gain, soften, gain1, gain2, q1, q2;
    var chain, src;
    var verb_time, verb_damp;
    var envFollower;
    var sines;
    // var scale = Scale.major(\partch);

    src = HPF.ar(InFeedback.ar(\inbus.kr(0), 2), \hpf_in.kr(50));
    envFollower = Amplitude.kr(src, attackTime: 0.1, releaseTime: 0.2, mul: 1.0, add: 0.0);

    mod = \mod.kr(0.2, spec:[0,10]).lag;
    omega = \omega.kr(0.0002, spec:ControlSpec(0.0001, 1, \exponential)).lag;
    damp = \damp.kr(0.01, spec:ControlSpec(0.0001, 1, \exponential)).lag + envFollower;
    rate = \rate.kr(0.03, spec:[0, 5]).lag;
    gain = \gain.kr(1.4, spec:[0, 3.5]).lag;
    soften = \soften.kr(1, spec:[0, 5]).lag;
    gain1 = \gain1.kr(1.5, spec:[0.0, 2.0, \lin]).lag;
    gain2 = \gain2.kr(1.5, spec:[0.0, 2.0, \lin]).lag;
    q1 = \q.kr(20, spec:ControlSpec(2.5, 800, \exponential)).lag3(1);

    pitch = \pitchShift.kr(0.25, spec: [0.05,2.0]).lag;

    chain = ~initChain.(numPartials: 24, freq: 440);
    // chain = ~extractSines.(chain, src, freqLag: \freqLag.kr(0.3), ampLag: \ampLag.kr(0.5), order: 1, transpose: 0, winSize: 1024, fftSize: 4096, hopSize: 4, numSines: 6);
    chain = ~extractSines_smooth.(chain, src, freqLag: \freqLag.kr(0.1), ampLag: \ampLag.kr(1), order: 1, transpose: 0.01, thresh: envFollower, numSines: 6);
    
    // chain = ~quantizePartials.(chain, scale, \quantize.kr(1), 60.midicps);
    // chain = ~extractSines_smooth.(chain, src, freqLag: \freqLag.kr(0.01), ampLag: \ampLag.kr(1), order: 0, transpose: 0, thresh: \thresh.kr(0), numSines: 24);
    
    chain = ~addLimiter.(chain);
    freq1 = chain[\freqs] * pitch;
    freq1 = freq1.clip(20, 20000);
    gain1 = chain[\amps] * gain1;

    freq2 = (freq1 * Array.rand(freq1.size, 0.95,1.0)).clip(0, 20000);

    // q = q ! freq1.size;
    // q1 = chain[\amps] * q1;
    // q1 = Array.rand(freq1.size, 0.95,1.0) * q1;
    q2 = Array.rand(freq1.size, 0.95,1.0) * q1;

    q1=q1.max(10);
    q2=q2.max(10);

    sig = GutterSynth.ar(
        gamma:         mod,
        omega:         omega,
        c:             damp,
        dt:         rate,
        singlegain: gain,
        smoothing:  soften,
        togglefilters: 1,
        distortionmethod: \distortionmethod.kr(1, spec: [0,4,\lin,1]),
        oversampling: 2,
        enableaudioinput: 0,
        // audioinput: src.exprange(100.0,2500.0),
        // audioinput: src,
        audioinput: SinOsc.ar(chain[\freq]),
        // audioinput: SinOsc.ar(SinOsc.ar(LFNoise2.ar(30)*100).exprange(100.0,2500.0)),
        gains1:     gain1,
        gains2:     gain2,
        freqs1:     `freq1,
        qs1:         `q1,
        freqs2:     `freq2,
        qs2:         `q2,
    );

    sig=Splay.ar(sig, 1, levelComp: true);

    // sig = PitchShift.ar(sig, pitchRatio:2, timeDispersion: 0.01);

    sig=HPF.ar(sig,\hpf_out.kr(100));
    // sig = Pan2.ar(sig, \pan.kr(0));

    verb_time = LFNoise2.kr(0.3, 0.1, 1.03);
    verb_damp = LFNoise2.kr(0.2).range(0, 0.7);

    sig = MiVerb.ar(sig, verb_time, \verbMix.kr(0.1), verb_damp, 0.1);
    
    sig = Limiter.ar(sig);
    sig = sig * \amp.kr(1);
    sig = sig * \masterGain.kr(1);
    sig = sig.sanitize();
    Out.ar(\out.kr(0), sig);
}).add;

// Ndef(\gutter)[999] = \pset -> Pbind(
//     \dur, 0.01, 
//     \amp, ~knob.(0).lag(0.1),
//     \verbMix, ~knob.(1).lag(2),

//     \omega, ~knob.(2).linlin(0,1, 0.001, 1),
//     \mod, ~knob.(3).linlin(0,1,0.01, 10),
//     \rate, ~knob.(4).linlin(0,1,0.01, 5),

//     \gain, ~slider.(0).linlin(0,1,0,3.5),
//     \gain1, ~slider.(1).linlin(0,1,0,2),
//     \gain2, ~slider.(2).linlin(0,1,0,2),

//     \damp, ~slider.(3).linexp(0,1,0.01, 1),
//     \soften, ~slider.(4).linlin(0,1,0,5),
//     \q, ~slider.(5).linexp(0,1,2.5,800),
//     \pitchShift, ~slider.(6).linlin(0,1,0.05, 2),
    
// );

b = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./feedback cymbals/feedback cymbals-4.wav");
~gutter = Bus.audio(s, 2);
Ndef(\guitar, \input).set(\in, 0, \amp, 1, \gain, -12).play(~gutter);

// Ndef(\sample, {PlayBuf.ar(2, \buf.kr(0), startPos: \pos.kr(10000), rate: \rate.kr(0), loop: \loop.kr(0)) * \gain.kr(0).dbamp;});
// Ndef(\sample).fadeTime = 0;
// Ndef(\sample).set(\buf, b, \pos, 0, \rate, 0.5, \loop, 1, \gain, -12).play([~gutter.channels]);

Ndef(\gutter, \gutter_tracker).set(\inbus, ~gutter).play(~bus2);
// Ndef(\gutter)[999] = \pset -> Pbind(
//     \dur, 0.01, 

//     \amp, 1,
//     \verbMix, 0.05,

//     \omega, 0.125,
//     \mod, 2,
//     \rate, 0.01,

//     \gain, 0.1,
//     \gain1, Pseg(Pseq([0,0.4],inf), 4, \lin, inf),
//     \gain2, Pseg(Pseq([0.4,0],inf), 4, \lin, inf),

//     \damp, 1,
//     \soften, 0.1,
//     \q, 2.5,
//     \pitchShift, 2,
//     \masterGain, 24
// );
    Ndef(\gutter)[999] = \pset -> Pbind(
        \dur, 0.01,
        \amp, 1,
        \verbMix, 0,

        \gain, ~slider.(0).linexp(0,1,0.001,3.5),
        \gain1, ~slider.(1).linexp(0,1,0.001,2),
        \gain2, ~slider.(2).linexp(0,1,0.001,2),

        \omega, 0.5,
        \rate, ~slider.(3).linlin(0,1,0,0.02),
        // \rate, 0.01,
        \mod, ~slider.(4).linlin(0,1,0,2),


        \damp, ~slider.(7),
        \soften, 0.1,
        \q, 3.5,
        \pitchShift, ~slider.(6) * 2,
        \masterGain, 12
    );
)



Ndef(\gutter).gui;

 
(
SynthDef(\fftfilterbank, {    
    var size, buf, in, chain, low, high, wipe, freq, sig, both, offset;
    in = WhiteNoise.ar(0.1);
    
    size = (2**11).asInteger;
    buf = LocalBuf(size);
    chain = FFT(buf, in);    
    
    freq = MouseX.kr(500, 5000, 1); // crossover frequency in Hz    
    wipe = freq/(s.sampleRate/2.0); // in range [-1,1]
    
    offset = 0.01;
    // Because of this offset, there is a gap  in the spectrum.
    // The gap is `(2*offset)*(s.sampleRate/2.0) == 480` Hz wide.
    // Look at the spectrum with `FreqScope.new`.
    low = PV_BrickWall(chain, (-1+wipe) - offset);
    high = PV_BrickWall(chain, wipe + offset);

    // Insert more processing here

    both = PV_Add(low, high);
    sig = IFFT(both);
    Out.ar(0, Pan2.ar(sig, 0.0));
}).add;
)

(\instrument: \fftfilterbank).play;


z = Buffer.read(s, Platform.resourceDir +/+ "sounds/a11wlk01.wav");

(
x = SynthDef(\specMap, {arg sndBuf, freeze = 0;
    var a, b, chain1, chain2, out;
    var buf = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./Field recs/drain paris audrey.wav");
    a = LocalBuf.new(2048);
    b = LocalBuf.new(2048);
    chain1 = FFT(a, SoundIn.ar(\in.kr(0)!2)); // to be filtered
    chain2 = FFT(b, PlayBuf.ar(2, buf, 1, loop: 1));
    // mouse x to play with floor.
    chain1 = PV_SpectralMap(chain1, chain2, 0, freeze, MouseX.kr(-1, 1), -1);
    out = IFFT(chain1);
    Out.ar(0, out.dup);
}).play(s, [\sndBuf, z, \freeze, 0]) 
)

x.set(\freeze, 1)
x.set(\freeze, 0);

x.free;

z.free;