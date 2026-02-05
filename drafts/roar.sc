(
    SynthDef(\roar, {
        var sig, sigDry, sigWet, sigShaped, feed;
        var tone, toneFreq, toneComp, toneAmpLo, toneAmpHi, drive, bias, amount;
        var filterFunc, filterFreq, filterLoHi, filterBP, filterRes, filterBW, filterPre;
        var feedAmt, feedFreq, feedBW, feedDelay, feedGate;
        
        drive       = \drive.kr(spec:ControlSpec(       0, 48,      default: 14         )).dbamp;
        tone        = \tone.kr(spec:ControlSpec(        -1, 1,      default:-0.4        ));
        toneFreq    = \toneFreq.kr(spec:ControlSpec(    20, 20000,  default: 5520       ));
        toneComp    = \toneComp.kr(spec:ControlSpec(    0, 1,       default: 1          ));
        amount      = \drywet.kr(spec:ControlSpec(      0, 1,       default: 0.8        ));
        bias        = \bias.kr(spec:ControlSpec(        -1, 1,      default: 0.0        ));
        
        filterFreq  = \filterFreq.kr(spec:ControlSpec(  20, 20000,  default: 12800      ));
        filterLoHi  = \filterLoHi.kr(spec:ControlSpec(  -1, 1,      default: -1         ));
        filterBP    = \filterBP.kr(spec:ControlSpec(    0, 1,       default: 0.2        ));
        filterRes   = \filterRes.kr(spec:ControlSpec(   0, 1,       default: 0.3        ));
        filterBW    = \filterBW.kr(spec:ControlSpec(    0, 4,       default: 0.5        ));
        filterPre   = \filterPre.kr(spec:ControlSpec(   0, 1,       default: 1          ));
        
        feedAmt     = \feedAmt.kr(spec:ControlSpec(     -90, 12,    default: 14         )).dbamp;
        feedFreq    = \feedFreq.kr(spec:ControlSpec(    20, 20000,  default: 80         ));
        feedBW      = \feedBW.kr(spec:ControlSpec(      0, 4,       default: 0.1        ));
        feedDelay   = \feedDelay.kr(spec:ControlSpec(   0, 4,       default: 1/6        )) - ControlDur.ir;
        feedGate    = \feedGate.kr(spec:ControlSpec(    0.02, 0.3,  default: 0.1        ));
        
        toneAmpLo   = tone.lincurve(-1.0, 1.0, 2.0, 0.0, -0);
        toneAmpHi   = tone.lincurve(-1.0, 1.0, 0.0, 2.0,  0);
        
        sig = InFeedback.ar(\inbus.kr(0), 2);      
        // WET TONE
        sigWet = sig
            |> BHiShelf.ar(_,  toneFreq, 1, toneAmpHi.ampdb)
            |> BLowShelf.ar(_, toneFreq, 1, toneAmpLo.ampdb);
        
        // DRY TONE
        sigDry = sig
            |> BHiShelf.ar(_,  toneFreq, 1, 0)
            |> BLowShelf.ar(_, toneFreq, 1, 0);
        
        // Dry should be silent if tone = 0, else it should "make up" 
        // the attenuation from the shelf filters? Use no-op filters on the dry
        // signal so delay from filter matches wet signal?
        sigDry = (sigDry - sigWet);
        
        // FEEDBACK
        feed = LocalIn.ar(2);
        feed = feed
            *> feedAmt
            |> BBandPass.ar(_, feedFreq, feedBW)
            |> DelayC.ar(_, 4, feedDelay)
            |> LeakDC.ar(_)
            *> Amplitude.ar(sig, 0.01, feedGate);
            
        // FILTER
        // filterLoHi blends between a lowpass and highpass
        // filterBP blends between the lo-hi signal and a bandpass
        filterFunc = {
            |sig|
            blend(
                blend(
                    BLowPass.ar(sig, filterFreq, filterRes),
                    BHiPass.ar(sig, filterFreq, filterRes),
                    filterLoHi.linlin(-1, 1, 0, 1)
                ),
                BBandPass.ar(sig, filterFreq, filterBW),
                filterBP
            )
        };
        
        // SHAPE: PRE-FILTER
        // filterPre blends between filtering befor the shape stage, or after
        sigShaped = sigWet + feed;
        sigShaped = blend(sigShaped, filterFunc.(sigShaped), filterPre);
        
        // SHAPE
        sigShaped = sigShaped
            *> drive
            +> bias
            // |> tanh(_);
            |> SoftClipAmp8.ar(_, drive);
            // |> SmoothFoldQ.ar(_, -1, 1, 0.8, 0.5);
        
        // SHAPE: POST-FILTER
        sigShaped = blend(sigShaped, filterFunc.(sigShaped), 1 - filterPre);
        LocalOut.ar(sigShaped);
        
        sigWet = blend(sigWet, sigShaped, amount);
        
        sig = sigWet + (toneComp * sigDry);
        sig = sig * \gain.kr(0).dbamp;
        Out.ar(\out.kr(0), \amp.kr(1) * sig * [1, 1]);
    }).add;
)
    
(
// \roarTest.asSynthDesc.controls.collect({
//     |c|
//     "%%,%".format(
//         $\\,
//         c.name, 
//         c.defaultValue
//             .round(0.01)
//             .asString
//             .padLeft(20 - c.name.asString.size.postln)
//     )
// }).join(",\n");

a = Buffer.read(s,"/Users/aelazary/Desktop/Samples etc./Silent Hill/Drum Loops/Silent Hill 3/Queen of the Rodeo/070 gtrspill 5 001.wav");

Pdef(\roadTestControls, Pbind(
    \drive,            6.0,
    \tone,            -0.2,
    \toneFreq,       820.0,
    \toneComp,         0.7,
    \amount,           1,
    \bias,             0.1,
    \filterFreq,    5800.0,
    \filterLoHi,      -0.7,
    \filterBP,         0.5,
    \filterRes,        0.7,
    \filterBW,         0.8,
    \filterPre,        0.0,
    \feedAmt,          5.0,
    \feedFreq,       20.0,
    \feedBW,             2,
    \feedDelay,       1/60,
    \feedGate,         1,
    \buffer,           0.0,
));

Pdef(\roadTest, Pmono(
    \roar,
    \dur, 1/4,
    \buffer, a,
    \amp, -3.dbamp,
) <> Pdef(\roadTestControls)).play
)