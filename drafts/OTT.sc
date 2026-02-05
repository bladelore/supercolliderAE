(
~multiband=Bus.audio(s,2);
Pdef(\multiband,
    Pmono(\multiBandComp,
        \amp, 1,
        \inbus, ~multiband,
        \addAction, \addToTail,
        \callback, { Pdefn(\fxid, ~id) },
    )
);
)

(
    var ampMin = -70;
    var ampMax = 36;
    var compressFunc = {
        |isSynth, amp, aboveRatio, belowRatio, ratioScale, aboveThreshold, belowThreshold, knee, curve, expandMax, plot=false|
        var compressAmt, expandAmt;
        var kneeStart, kneeSlopeStart, kneeEnd, kneeSlopeEnd;
        var zero, curveMult, hermCurve;
        var compressCurve;
        
        zero = isSynth.if({ DC.ar(0) }, { 0 });
        curveMult = isSynth.if(
            {
                aboveRatio > 1
            },
            {
                (aboveRatio > 1).if(1, 0)
            }
        );
        
        aboveRatio = (1 / aboveRatio) * 2.pow(ratioScale.neg);
        belowRatio = (1 / belowRatio) * 2.pow(ratioScale.neg);
        
        knee = min(knee, (aboveThreshold - belowThreshold).abs / 2);
        
        compressAmt = (amp - aboveThreshold);
        compressAmt = compressAmt.linlin(knee.neg, knee, 0, knee) + (compressAmt - knee).clip(0, inf);
        aboveRatio = aboveRatio / (1 + (curve * compressAmt * curveMult));
        compressAmt = compressAmt * (1 - aboveRatio);
        
        expandAmt = (belowThreshold - amp);
        expandAmt = expandAmt.linlin(knee.neg, knee, 0, knee) + (expandAmt - knee).clip(0, inf);
        expandAmt = expandAmt * (1 - belowRatio);
        expandAmt = expandAmt.clip(0, expandMax);
        
        [compressAmt, expandAmt];
    };

    SynthDef(\multiBandComp, 
        {
            // |numChannels=2|
            var numChannels=2;
            var in, bands, rms, peak, letters;
            var thresholdAdd, aboveRatioMul, belowRatioMul, postAmp;
            var attack, decay, solo, preGain, metadata, gain, knee, bypassAll, ratioScale, expandMax;
            var lag;
            var fLow, fMid, fHigh;
            var db = true;
            
            // in = \in.ar(0 ! numChannels);
            // in = In.ar(\in.ar, 2);

            in = InFeedback.ar(\inbus.kr(0), 2);
            
            
            letters 		= ["a", "b", "c", "d"];
            
            lag				= \lagTime.kr(spec:ControlSpec(0, 10, warp:12, default:10));
            preGain			= \preGain.kr(spec:ControlSpec(ampMin, ampMax, \lin, default:0)).lag(lag);
            attack 			= \attack.kr(spec:ControlSpec(0, 0.2, \lin, default: 0.2)).lag(lag);
            decay 			= \decay.kr(spec:ControlSpec(0, 1, \lin, default: 1)).lag(lag);
            solo			= \solo.kr(spec:ControlSpec(-1, 4, \lin, default: -1)).lag(lag);
            gain			= \gain.kr(spec:ControlSpec(ampMin, ampMax, \lin, default: 0)).lag(lag);
            bypassAll       = \bypasssAll.kr(spec:ControlSpec(0, 1, \lin, default: 0)).lag(lag);
            knee			= \knee.kr(spec:ControlSpec(0, 12, default:3)).lag(lag);
            expandMax		= \expandMax.kr(spec:ControlSpec(0, 36, default:36)).lag(lag);
            ratioScale	 	= \ratioScale.kr(spec:ControlSpec(-4, 4, default:4));
            
            fLow			= \fLow.kr(spec:\freq.asSpec.copy.default_(100));
            fMid			= \fMid.kr(spec:\freq.asSpec.copy.default_(700));
            fHigh			= \fHigh.kr(spec:\freq.asSpec.copy.default_(4000));
            
            rms				= \rms.kr(spec:ControlSpec(0, 1, default:1));
            rms 			= rms > 0;
            
            knee			= max(knee, 0.00001); // avoid divide-by-zero
            
            metadata 		= Array.newClear((4*4));
            
            in = in * preGain.dbamp;
            
            bands = BandSplitter4.ar(in, fLow, fMid, fHigh);
            bands = bands.collect {
                |band, i|
                var amp, ampDiff, preGain, belowThreshold, aboveThreshold, belowRatio, aboveRatio, bypass;
                var targetAmp, gain, compressAmt, expandAmt, curve;
                var mute, zero = DC.ar(0);
                
                preGain			= "preGain_%".format(letters[i]).asSymbol.kr(spec:ControlSpec(ampMin, ampMax, \lin, default: 0));
                aboveThreshold 	= "aboveThreshold_%".format(letters[i]).asSymbol.kr(spec:ControlSpec(ampMin, ampMax, \lin, default: -10));
                belowThreshold 	= "belowThreshold_%".format(letters[i]).asSymbol.kr(spec:ControlSpec(ampMin, ampMax, \lin, default: -40));
                curve			= "curve_%".format(letters[i]).asSymbol.kr(spec:ControlSpec(0, 64, default:0));
                aboveRatio 		= "aboveRatio_%".format(letters[i]).asSymbol.kr(spec:ControlSpec(0.1, 200, \exp, default: 3));
                belowRatio 		= "belowRatio_%".format(letters[i]).asSymbol.kr(spec:ControlSpec(0.1, 200, \exp, default: 1));
                gain 			= "gain_%".format(letters[i]).asSymbol.kr(spec:ControlSpec(ampMin, ampMax, \lin, default: 0));
                bypass 			= "bypass_%".format(letters[i]).asSymbol.kr(spec:ControlSpec(0, 1, \lin, default: 0));
                bypass      	= max(bypassAll, bypass) > 0;
                
                #preGain, gain = K2A.ar([preGain, gain]).dbamp.lag(lag);
                
                mute = (solo >= 0) * ((solo - i).abs > 0.001);
                band = band * (mute < 1);
                band = band * blend(preGain, DC.ar(1), bypass);
                
                amp = blend(
                    band.abs,
                    RMS.ar(band),
                    rms
                );
                amp = ArrayMax.ar(amp)[0].ampdb.clip(-120, 24);
                
                #compressAmt, expandAmt = compressFunc.(
                    true,
                    amp, aboveRatio, belowRatio, 
                    ratioScale, aboveThreshold, belowThreshold, 
                    knee, curve, expandMax
                );
                compressAmt = blend(compressAmt, zero, bypass);
                expandAmt = blend(expandAmt, zero, bypass);
                
                metadata[i*4] = amp;
                metadata[i*4+1] = (expandAmt - compressAmt).lagud(attack, decay);
                metadata[i*4+2] = (amp + (expandAmt - compressAmt).lagud(attack, decay));
                metadata[i*4+3] = (amp + gain.ampdb + (expandAmt - compressAmt).lagud(attack, decay));
                
                (mute <= 0) * (
                    band * gain * (expandAmt - compressAmt).lagud(attack, decay).dbamp
                );
            };
            
            bands = K2A.ar(gain).dbamp.lag(lag) * bands.sum;    
            
            postAmp = blend(
                bands.abs,
                RMS.ar(bands),
                rms
            );
            postAmp = ArrayMax.ar(postAmp)[0].ampdb.clip(-120, 24);
            
            metadata = metadata.add(postAmp);
            
            SendReply.ar(Impulse.ar(30), '/compressor', metadata, \replyId.kr(0));
            
            // bands.assertChannels(numChannels);
            
            Out.ar(\out.kr(0), bands);
    }).add;
)

