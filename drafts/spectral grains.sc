(
SynthDef(\grains, { |sndBuf|

    var numChannels = 8;

    var reset, events, voices;

    var tFreqMod, tFreq;
    var overlapMod, overlap;
    var posRateMod, posRate;
    var grainRateMod, grainRate;

    var grainWindows, accumulator, grainPhases, pos;
    var sigs, sig;

    reset = Trig1.ar(\reset.tr(0), SampleDur.ir);

    tFreqMod = LFDNoise3.ar(\tFreqMF.kr(1));
    tFreq = \tFreq.kr(1) * (2 ** (tFreqMod * \tFreqMD.kr(0)));

    events = SchedulerCycle.ar(tFreq, reset);

    overlapMod = LFDNoise3.ar(\overlapMF.kr(0.3));
    overlap = \overlap.kr(1) * (2 ** (overlapMod * \overlapMD.kr(0)));

    voices = VoiceAllocator.ar(
        numChannels: numChannels,
        trig: events[\trigger],
        rate: events[\rate] / overlap,
        subSampleOffset: events[\subSampleOffset],
    );

    grainWindows = HanningWindow.ar(
        phase: voices[\phases],
        skew: \windowSkew.kr(0.5),
    );

    ///////////////////////////////////////////////////////////////////////////////////

    posRateMod = LFDNoise3.ar(\posRateMF.kr(0.3));
    posRate = \posRate.kr(1) * (1 + (posRateMod * \posRateMD.kr(0)));

    pos = Phasor.ar(
        trig: DC.ar(0),
        rate: posRate * BufRateScale.kr(sndBuf) * SampleDur.ir / BufDur.kr(sndBuf),
        start: \posLo.kr(0),
        end: \posHi.kr(1)
    );
    pos = Latch.ar(pos, voices[\triggers]) * BufFrames.kr(sndBuf);

    ///////////////////////////////////////////////////////////////////////////////////

    grainRateMod = LFDNoise3.ar(\grainRateMF.kr(0.3));
    grainRate = \grainRate.kr(1) * (2 ** (grainRateMod * \grainRateMD.kr(0)));

    accumulator = RampAccumulator.ar(
        trig: voices[\triggers],
        subSampleOffset: events[\subSampleOffset]
    );
    grainPhases = Latch.ar(grainRate, voices[\triggers]) * accumulator;

    ///////////////////////////////////////////////////////////////////////////////////

    sigs = BufRd.ar(
        numChannels: 1,
        bufnum: sndBuf,
        phase: grainPhases + pos,
        loop: 1,
        interpolation: 4
    );

    sigs = sigs * grainWindows;

    sigs = PanAz.ar(2, sigs, \pan.kr(0));
    sig = sigs.sum;

    sig = sig * \amp.kr(-25).dbamp;

    sig = sig * Env.asr(0.001, 1, 0.001).ar(Done.freeSelf, \gate.kr(1));

    sig = LeakDC.ar(sig);
    sig = Limiter.ar(sig);
    Out.ar(\out.kr(0), sig);
}).add;
)

(
~maxGrains = 50;
~fftSize = 4096*8;
d = Buffer.read(s, "/Users/aelazary/Projects/feedback cymbals 2025-09-30 Project/feedback cymbals 2025-09-30.wav");
// d = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./contact mics/skateboard 1.wav");
e = Array.fill(~maxGrains, {Buffer.alloc(s, ~fftSize)});
e.do{|item| item.zero};
)

(
{
    var numChannels = 50;
    var reset, events, voices, grainWindows, overlap, overlapMod, tFreq, tFreqMod, posRate, posRateMod;
    var pitchRatio, pitchMod;
    var trig, pos, chain, accumChain;
    var polarity, sig;

    var delayBuf, fbBuf;
    var activeVoices;
    
    reset = Trig1.ar(\reset.tr(0), SampleDur.ir);

    tFreqMod = LFDNoise3.ar(\tFreqMF.kr(1));
    tFreq = \tFreq.kr(100) * (2 ** (tFreqMod * \tFreqMD.kr(2)));

    events = SchedulerCycle.ar(tFreq, reset);

    overlapMod = LFDNoise3.ar(\overlapMF.kr(1));
    overlap = \overlap.kr(16) * (2 ** (overlapMod * \overlapMD.kr(0)));

    voices = VoiceAllocator.ar(
        numChannels: numChannels,
        trig: events[\trigger],
        rate: events[\rate] / overlap,
        subSampleOffset: events[\subSampleOffset],
    );

    // grainWindows = HanningWindow.ar(
    //     phase: voices[\phases],
    //     skew: \windowSkew.kr(0.5),
    // );

    grainWindows = TukeyWindow.ar(
        phase: voices[\phases],
        skew: \windowSkew.kr(0.5),
        width: \windowWidth.kr(0.9)
    );

    posRateMod = LFDNoise3.ar(\posRateMF.kr(0.3));
    posRate = \posRate.kr(0.1) * (1 + (posRateMod * \posRateMD.kr(0)));

    pos = Phasor.ar(
        trig: DC.ar(0),
        rate: posRate * BufRateScale.kr(d) * SampleDur.ir / BufDur.kr(d),
        start: \posLo.kr(0),
        end: \posHi.kr(1)
    );

    pos = Latch.ar(pos, voices[\triggers]) * BufFrames.kr(d);
    
    //fft
    chain = BufFFTTrigger2(e, voices[\triggers]);
    chain.poll();
    
    pitchMod = LFDNoise3.ar(\pitchMod.kr(1)) * \pitchModDepth.kr(0.01);
    pitchRatio = (\midipitch.kr(0) + pitchMod).midiratio;

    chain = BufFFT_BufCopy(chain, d, pos, BufRateScale.kr(d) * pitchRatio);

    //set to rect window because we are using grainWindows later
    chain = BufFFT(chain, wintype: -1);
    //whatever
    // chain = PV_MagGate(chain, TRand.kr(0, 1, voices[\triggers]), TRand.kr(0, 50, voices[\triggers]));
    // chain = PV_MagGate(chain, MouseY.kr(-1, 1), MouseX.kr(0, 50));
    // chain = PV_MagAbove(chain, MouseY.kr(0, 300));
    // chain = PV_MagBelow(chain, MouseX.kr(0, 300));
    // chain = PV_MagAbove(chain,  TRand.kr(0, 300, voices[\triggers]));
    // chain = PV_MagClip(chain, MouseY.kr(0, 300));
    // chain = PV_LocalMax(chain, TRand.kr(0, 300, voices[\triggers]));
    // chain = PV_MagFreeze(chain, voices[\triggers]);
    // chain = PV_MagSmooth(chain, 1 - MouseX.kr(1, 0.00001, 1));

    // chain = PV_BinScramble(chain, MouseX.kr, 0.1, MouseY.kr > 0.5);
    // chain = PV_MagSmear(chain, MouseX.kr(0, 100));
    
    // delayBuf = LocalBuf(~fftSize*0.5);
    // fbBuf = LocalBuf(~fftSize*0.5);
    // chain = PV_BinDelay(chain, 0.5, delayBuf, fbBuf, 0.25);
    
    // chain = PV_RectComb(
    //     chain, 
    //     TRand.kr(0, 32, voices[\triggers]), 
    //     TRand.kr(0, 1, voices[\triggers]), 
    //     MouseX.kr(0,1),
    // );
    
    // chain = PV_MagGate(chain, TRand.kr(-1, 1, voices[\triggers]), TRand.kr(0, 50, voices[\triggers]));
    chain = PV_Compander(chain, TExpRand.kr(0.1, 10, voices[\triggers]), 0.1, 1.0);
    // chain = PV_Compander(chain, 5, 0.1, 1.0);
    
    //turn on or off
    accumChain = LocalBuf(~fftSize);
    accumChain = PV_AccumPhase(accumChain, chain);
    accumChain = PV_BinGap(
        accumChain, 
        TRand.kr(0, 1000, voices[\triggers]),
        TRand.kr(0, 1000, voices[\triggers])
    );
    chain = PV_CopyPhase(chain, accumChain);
    
    sig = BufIFFT(chain, wintype: 0);
    
    //set polarity w polarityMod 0-1
    polarity = ~multiVelvet.(
        voices[\triggers], 
        density: \density.kr(0.5), 
        bias: 1 - \polarityMod.kr(1),
        // stereo: 0
    );

    // activeVoices = polarity.abs.sum()[0];
    
    sig = sig * grainWindows * polarity;

    // polarity.poll(2);

    // // sig.poll();
    // // sig = sig * sqrt(activeVoices).reciprocal;
    sig = sig.softclip;
    // // sig;
    Mix(Pan2.ar(sig, TRand.kr(-1, 1, voices[\triggers])));
    // Mix(sig);
}.play
)

Miscellaneous

(
SynthDef(\spectralGrains1, {|srcbuf, fftSize=4096|
    var numChannels = 50;
    var reset, events, voices, grainWindows;
    var overlap, overlapMod, tFreq, tFreqMod, posRate, posRateMod, pitchRatio, pitchMod;
    var trig, pos, chain, accumChain;
    var polarity, sig;
    var feedback, in, ptr, prev, current, fbOut;
    
    feedback = (LocalIn.ar(1) * \feedback.ar(0) * 0.9);
	in = InFeedback.ar(\inbus.kr(0), 1);

    ptr = Phasor.ar(0, \recRate.kr(1), 0, BufFrames.kr(srcbuf));
	prev = BufRd.ar(1, srcbuf, ptr);
	current = XFade2.ar(in + feedback, prev, \overdub.kr(0));
	BufWr.ar(current, srcbuf, ptr);
    
    reset = Trig1.ar(\reset.tr(0), SampleDur.ir);

    tFreqMod = LFDNoise3.ar(\tFreqMF.kr(1));
    tFreq = \tFreq.kr(10) * (2 ** (tFreqMod * \tFreqMD.kr(0)));

    events = SchedulerCycle.ar(tFreq, reset);

    overlapMod = LFDNoise3.ar(\overlapMF.kr(1));
    overlap = \overlap.kr(1) * (2 ** (overlapMod * \overlapMD.kr(0)));

    voices = VoiceAllocator.ar(
        numChannels: numChannels,
        trig: events[\trigger],
        rate: events[\rate] / overlap,
        subSampleOffset: events[\subSampleOffset],
    );

    grainWindows = TukeyWindow.ar(
        phase: voices[\phases],
        skew: \windowSkew.kr(0.5),
        width: \windowWidth.kr(0.9)
    );

    posRateMod = LFDNoise3.ar(\posRateMF.kr(0.3));
    posRate = \posRate.kr(1) * (1 + (posRateMod * \posRateMD.kr(0)));

    pos = Phasor.ar(
        trig: DC.ar(0),
        rate: posRate * BufRateScale.kr(srcbuf) * SampleDur.ir / BufDur.kr(srcbuf),
        start: \posLo.kr(0),
        end: \posHi.kr(1)
    );

    pos = Latch.ar(pos, voices[\triggers]) * BufFrames.kr(srcbuf);

    pitchMod = LFDNoise3.ar(\pitchMod.kr(1)) * \pitchModDepth.kr(0);
    pitchRatio = (\midipitch.kr(0) + pitchMod).midiratio;

    //fft
    chain = BufFFTTrigger2(\specbuf.kr(0 ! numChannels), voices[\triggers]);

    chain = BufFFT_BufCopy(chain, srcbuf, pos, BufRateScale.kr(srcbuf) * pitchRatio);
    chain = BufFFT(chain, wintype: 0);
    
    //whatever
    // chain = PV_MagGate(chain, \magThresh.kr(0), \magRemove.kr(0));
    // chain = PV_Compander(chain, \companderThresh.kr(5), 0.1, 1.0);
    chain = PV_MagAbove(chain, MouseY.kr(1, 0.001).linexp(0.001, 1, 0.001, 300));

    // chain = PV_RectComb(
    //     chain, 
    //     TRand.kr(0, 32, voices[\triggers]), 
    //     TRand.kr(0, 1, voices[\triggers]), 
    //     MouseX.kr(0,1),
    // );

    chain = PV_Compander(chain, TExpRand.kr(0.1, 10, voices[\triggers]), 0.1, 1.0);
    
    //turn on or off
    accumChain = LocalBuf(fftSize);
    accumChain = PV_AccumPhase(accumChain, chain);
    accumChain = PV_BinGap(
        accumChain, 
        TRand.kr(0, 1000, voices[\triggers]),
        TRand.kr(0, 1000, voices[\triggers])
    );
    chain = PV_CopyPhase(chain, accumChain);
    
    sig = BufIFFT(chain, wintype: 0);
    
    //set polarity w polarityMod 0-1
    polarity = ~multiVelvet.(voices[\triggers], \density.kr(1), 1 - \polarityMod.kr(1));
    sig = sig * grainWindows * polarity;
    sig = Mix(Pan2.ar(sig, TRand.kr(-1, 1, voices[\triggers])));

    // overdub feedback
	fbOut = sig.sum * 0.5;
	fbOut = LeakDC.ar(fbOut, 0.995);
	LocalOut.ar(fbOut);

	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
    sig = sig.softclip;
	Out.ar(\out.kr(0), sig);
}).add;
)

(
~spectralGrains=Bus.audio(s,2);

Pdef(\spectralGrains1,
    Pmono(\spectralGrains1,
        \amp, 1,
        \inbus, ~spectralGrains,
        \addAction, \addToTail,
        \callback, { Pdefn(\fxid, ~id) },
    )
);
)

{SoundIn.ar(\in.kr(0)!2)}.play;

(
    SynthDef(\input, {
        var sig;
        sig = SoundIn.ar(\in.kr(0)!2);
        // sig = DCompressor.ar(sig, threshold: -4);
        sig = sig * \gain.kr(1).dbamp;
        sig = sig * \amp.kr(1);
        Out.ar(\out.kr(0), sig);
    }).add;
)

(
~maxGrains = 50;
~fftSize = 4096*8;
~bufA = Buffer.alloc(s, ~fftSize*2);
~bufB = Buffer.alloc(s, s.sampleRate * 0.01);
~bufC = Buffer.alloc(s, s.sampleRate * 0.025);
~specBuf = Array.fill(~maxGrains, {Buffer.alloc(s, ~fftSize)});
~specBuf.do{|item| item.zero};
d = Buffer.readChannel(
    s,
    "/Users/aelazary/Projects/feedback cymbals 2025-09-30 Project/feedback cymbals 2025-09-30.wav", 
    channels:[0]
);
)



(
Pdef(\player,
    Pspawner({| sp |
        
        var sectionLength, sample, drum;
            
            sp.par(
                Pdef(\guitar,
                    Pmono(\input,
                        \in, 0,
                        \amp, 1,
                        \dur, 0.1,
                        \gain, -12,
                        // \out, [~spectralGrains, ~mainout],
                        \out, [~bus4],
                    )
                )
            );

            // sp.par(
            //     Pbindf(
            //         Pdef(\spectralGrains1),
            //         \srcbuf, ~bufA,
            //         \specbuf, [~specBuf],
            //         \fftSize, ~fftSize,
            //         \amp, 1,
            //         \dur, 0.1,
            //         \posRate, 0.1,
            //         \tFreq, 10,
            //         \tFreqMD, 0,
            //         \overlap, 12,
            //         \overdub, 0.5,
            //         \midipitch, -12,
            //         \gain, 24,
            //         \out, ~bus1
            //     )
            // );
        })).play(t);
)
