(
SynthDef(\input, {
	var sig;
	sig = SoundIn.ar(\in.kr(0)!2);
	sig = sig * \gain.kr(1).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\envfollow, { |gate=1|
    var sig;
    var in = InFeedback.ar(\inbus.kr(0), 2);
    sig = in;
	sig = EnvFollow.ar(sig.mean);
	// sig = sig * EnvGen.kr(\adsr.kr(Env.adsr(0.1,0.1,0.8,0.1)), gate, doneAction:2);
	// sig = Amplitude.ar(sig, \atk.kr(0.01), \rel.kr(0.1));
    sig = sig * \gain.kr(1);
    sig = sig * \mul.kr(1) + \add.kr(0);
	sig = sig * \amp.kr(1);
    Out.kr(\out.kr(0), sig);
}).add;

SynthDef(\comp, { arg out=0, gate=1;
    var sig;
    var in = InFeedback.ar(\inbus.kr(0), 2);
	sig = DCompressor.ar(
		in,
		sidechainIn: 0,
		sidechain: 0,
		ratio: \ratio.kr(6),
		threshold: \thresh.kr(-40),
		attack: \atk.kr(0.1),
		release: \rel.kr(100),
		makeup: 0.5,
		automakeup: \auto.kr(1)
	);
	sig = SelectX.ar(\mix.kr(1), [in, sig]);
	// sig = sig * EnvGen.kr(\adsr.kr(Env.adsr(0.1,0.1,0.8,0.1)), gate, doneAction:2);
    sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
    Out.ar(out, sig);
}).add;

SynthDef(\tape, {
	var in, sigmoid, x, y, y2, gainDiff, sig, postgain;
	in = InFeedback.ar(\inbus.kr(0), 2);
	//in = SinOsc.ar(440) * 0.01 ! 2;
	x = (in * \pregain.kr(30).dbamp) + \dcOffset.kr(0);
	sigmoid = \sigmoid.kr(1).neg * 100;
	y = (2 / (1 + exp(sigmoid * x))) - 1;
	//calculating...
	//norm_coeff = ((2 / (1 + exp(-2))) - 1);
	y2 = (y - 0.761594) / -98;
	postgain = \postgain.kr(0).dbamp;
	gainDiff = in-(y2*postgain);
	y = SelectX.ar(\autogain.kr(0), [y * postgain, y * gainDiff]);
	sig = SelectX.ar(\drywet.kr(1), [in, y]);
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\choruscompresseffect, {|out=0 gate= 1|
	var source = InFeedback.ar(\inbus.kr(0),2);
	var sig;
	var env = Linen.kr(gate, 0.1, 1, 0.1, 2);
	sig= Splay.ar(Array.fill(4,{
		var maxdelaytime=rrand(0.005,0.02);
		DelayC.ar(source[0], maxdelaytime,LFNoise1.kr(Rand(0.1,0.6),0.25*maxdelaytime,0.75*maxdelaytime) )
	}));
	sig = Compander.ar(4*(source + sig),source,0.4,1,4);
	sig = SelectX.ar(\mix.kr(1), [source, sig]);
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(out, sig);
	// XOut.ar(out, env, sig);
}).add;

SynthDef(\nhverb, { arg out=0, gate=1;
    var sig;
    var in = InFeedback.ar(\inbus.kr(0), 2);
    sig = in;
	sig = NHHall.ar(
		sig,
		rt60: \rt60.kr(1),
		stereo: \stereo.kr(0.5),
		lowFreq: \lowFreq.kr(200),
		lowRatio: \lowRatio.kr(0.5),
		hiFreq: \hiFreq.kr(4000),
		hiRatio: \hiRatio.kr(0.5),
		earlyDiffusion: \earlyDiffusion.kr(0.3),
		lateDiffusion: \lateDiffusion.kr(0.5),
		modRate: \modRate.kr(0.2),
		modDepth: \modDepth.kr(0.2)
	);
	sig = SelectX.ar(\mix.kr(1), [in, sig]);
	// sig = sig * EnvGen.kr(\adsr.kr(Env.adsr(0.1,0.1,0.8,0.1)), gate, doneAction:2);
    sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
    Out.ar(out, sig);
}).add;

SynthDef(\gverb, { arg out=0, gate=1;
    var sig;
    var in = InFeedback.ar(\inbus.kr(0), 2);
    sig = in;

	sig = GVerb.ar(
		sig,
		roomsize: \roomsize.kr(10),
		revtime: \revtime.kr(3),
		damping: \damping.kr(0.5),
		inputbw: \inputbw.kr(0.5),
		spread: \spread.kr(15),
		drylevel: 0,
		earlyreflevel: \earlyref.kr(0.7),
		taillevel: \tail.kr(0.5),
		maxroomsize: 300
	);

	sig = SelectX.ar(\mix.kr(1), [in, sig]);
	// sig = sig * EnvGen.kr(\adsr.kr(Env.adsr(0.1,0.1,0.8,0.1)), gate, doneAction:2);
    sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
    Out.ar(out, sig);
}).add;

SynthDef(\greyhole, { arg out=0, gate=1;
    var sig;
    var in = InFeedback.ar(\inbus.kr(0), 2);
    sig = in;

	sig = Greyhole.ar(sig,
        \dtime.kr(0.1),
        \damp.kr(0.1),
        \size.kr(1),
        \diff.kr(0.707),
        \fb.kr(0.1),
        \modDepth.kr(0.01),
        \modFreq.kr(2),
	);

	sig = SelectX.ar(\mix.kr(1), [in, sig]);
	// sig = sig * EnvGen.kr(\adsr.kr(Env.adsr(0.1,0.1,0.8,0.1)), gate, doneAction:2);
    sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
    Out.ar(out, sig);
}).add;

SynthDef(\echo,{|out=0, size = 0.5, offset = 0.012|
	var input = InFeedback.ar(\inbus.kr(0), 2);
	var feedback = LocalIn.ar(2);
	var sig = LeakDC.ar(feedback*\fb.kr(0.8) + input);
	sig = HPF.ar(sig, \hpf.kr(400));
	sig = LPF.ar(sig, \lpf.kr(5000));
	sig = sig.tanh;

	LocalOut.ar(DelayC.ar(sig, 1, LFNoise2.ar(12).range([size,size+offset],[size+offset,size])).reverse);

	sig = SelectX.ar(\mix.kr(1), [input, sig]);
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(out, sig);
}).add;

SynthDef(\combDelay,{|out=0, size = 0.5, offset = 0.012|
	var input = InFeedback.ar(\inbus.kr(0), 2);
	var sig = CombL.ar(input, 1, [size, size + (offset*size)], \fb.kr(0.8));
	sig = HPF.ar(sig, \hpf.kr(400));
	sig = LPF.ar(sig, \lpf.kr(5000));
	sig = sig.tanh;
	sig = SelectX.ar(\mix.kr(1), [input, sig]);
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(out, sig);
}).add;

SynthDef(\resonator, {
    var sig;
    var freq = \freq.kr(60);
    var atk = \atk.kr(0.01);
    var dec = \dec.kr(1) + atk;
    var in = InFeedback.ar(\inbus.kr(0), 2);
    in = LeakDC.ar(in, 0.995);

    // sig = Ringz.ar(in * \inGain.kr(-30).dbamp, freq, atk);
    // sig = Resonz.ar(in, 60, (1-bw).max(0.001));
    sig = Formlet.ar(in * \inGain.kr(-30).dbamp, freq, atk, dec);

    sig = (sig * \distort.kr(1)).tanh;
    sig = sig * \amp.kr(1) * \gain.kr(0).dbamp;
    sig = Splay.ar(sig, \stereo.kr(0));
    sig = sig.sanitize;
    Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\convolve_AB,{|out=0|
	var in_A = InFeedback.ar(\inbus_A.kr(0), 2);
	var in_B = InFeedback.ar(\inbus_B.kr(0), 2);
	var sig;
	var hpfIn = \hpfIn.kr(80);
	var hpfOut = \hpfOut.kr(80);
	in_A = HPF.ar(in_A, hpfIn);
	in_B = HPF.ar(in_B, hpfIn);
	sig = Convolution.ar(in_A, in_B, \framesize.kr(1024));

	sig = HPF.ar(sig, hpfOut);

	sig = Compander.ar(sig, sig,
		thresh: 0.5,
		slopeAbove: 0.1,
		clampTime: 0.01,
		relaxTime: 1,
	);
	// sig = sig.tanh;
	sig = SelectX.ar(\mix.kr(1), [in_A, sig]);
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(out, sig);
}).add;

SynthDef(\formantBankX, {
	var in, sig;
	var f1, f2, makeup;

	var f_table1, f_freq1, f_amp1, f_q1;
	var f_table2, f_freq2, f_amp2, f_q2;

	f_table1 = FormantTable.get(\tenorU);
	f_freq1 = f_table1[0];
	f_amp1 = f_table1[1];
	f_q1 = f_table1[2];

	f_table2 = FormantTable.get(\bassA);
	f_freq2 = f_table2[0];
	f_amp2 = f_table2[1];
	f_q2 = f_table2[2];

	in = InFeedback.ar(\inbus.kr(0), 2);

	f1 = BBandPass.ar(in, f_freq1.dup, f_q1) * f_amp1;
	f1 = Mix.new(f1);

	f2 = BBandPass.ar(in, f_freq2.dup, f_q2) * f_amp2;
	f2 = Mix.new(f2);

	sig = SelectX.ar(\blend.kr(0),[f1, f2]) * (24).dbamp;
	// sig = Mix.new(sig) * (24).dbamp;

	sig = SelectX.ar(\drywet.kr(1),[in, sig]);
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;


SynthDef(\formantBank, {
	var in, sig;
	var f_table, f_freq, f_amp,f_q, makeup;
	f_table = FormantTable.get(\bassU);
	f_freq = f_table[0];
	f_amp = f_table[1];
	f_q = f_table[2];

	in = InFeedback.ar(\inbus.kr(0), 2);
	sig = BBandPass.ar(in, f_freq.dup, f_q) * f_amp;
	sig = Mix.new(sig) * (24).dbamp;

	sig = SelectX.ar(\drywet.kr(1),[in,sig]);
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\pitchShift, {
	var in, sig;

	in = InFeedback.ar(\inbus.kr(0), 2);

	sig = PitchShift.ar(in,
			pitchRatio: \pitchRatio.kr(1),
			windowSize: \windowSize.kr(0.01),
			pitchDispersion: \pitchDispersion.kr(1),
			timeDispersion: \timeDispersion.kr(0.1)
	);

	sig = SelectX.ar(\drywet.kr(1), [in, sig]);

	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\vafilter, {
	var in, sig;
	var ftype, fselect;

	ftype = \type.kr(0);

	in = InFeedback.ar(\inbus.kr(0), 2);
	in = in * \ingain.kr(0).dbamp;

	sig = VadimFilter.ar(in, \freq.kr(500), \res.kr(0), ftype);

	sig = SelectX.ar(\drywet.kr(1), [in, sig]);

	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\vasem12, {
	var in, sig;

	in = InFeedback.ar(\inbus.kr(0), 2);
	in = in * \ingain.kr(0).dbamp;

	sig = VASEM12.ar(in, \freq.kr(500), \res.kr(0), \blend.kr(1).linlin(0,1,1,-1));

	sig = SelectX.ar(\drywet.kr(1), [in, sig]);

	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\lpf, {
	var in, sig;


	in = InFeedback.ar(\inbus.kr(0), 2);
	in = in * \ingain.kr(0).dbamp;

	sig = LPF.ar(in, \freq.kr(500));

	sig = SelectX.ar(\drywet.kr(1), [in, sig]);

	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

SynthDef(\EQstack, {
	var in, sig;
	in = InFeedback.ar(\inbus.kr(0), 2);

	sig = BLowShelf.ar(in, \lofreq.kr(500), 1, \lodb.kr(0));
	sig = BPeakEQ.ar(sig, \midfreq.kr(1200), 1, \middb.kr(0));
	sig = BHiShelf.ar(sig, \hifreq.kr(7000), 1, \hidb.kr(0));

	sig = SelectX.ar(\drywet.kr(1), [in, sig]);
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

~velvet = {|t, density, bias|

    var n = Dseries(0, 0, inf);

    var high = 1 - density;
    var low = density * 2;

    var nx1 = Diwhite(0, 1, inf);
    //bias
    var nx2 = (Dwhite(0, 1, inf) < (0.5 + (bias * 0.5))).if(1, -1);

    var out = Dswitch1([0, nx2], nx1 >= high);

	Demand.ar(t, 0, out);
};

SynthDef(\liveGrain_mono, { |bufnum|
	var polarity, polarityProb, pan, grains, fb;
	var ptr, prev, current;
	//bufFrames
	var bufFrames = BufFrames.ir(bufnum);
	//in
	var feedback = (LocalIn.ar(1) * \feedback.ar(0) * 0.9);
	var in = InFeedback.ar(\inbus.kr(0), 1);
	//trigger and duration
	var trigRate = \trigRate.kr(100);
	var t = Impulse.ar(trigRate);
	var dur = trigRate.reciprocal * \overlap.kr(1) * \rate.kr(1).reciprocal;
	//sample bounds
	var startsamp = \startsamp.kr(0) * bufFrames;
	var endsamp = \endsamp.kr(1) * bufFrames;
	var sampsDur = endsamp - startsamp;
	//readback phasor
	var granPhasor = Phasor.ar(
		rate: (1 / bufFrames * BufRateScale.kr(bufnum)) * \posRate.kr(1),
		start: startsamp,
		end: endsamp,
		//trig: gate,
		resetPos: startsamp
	);
	//overdub input
	ptr = Phasor.ar(0, \recRate.kr(1), 0, bufFrames);

	prev = BufRd.ar(1, bufnum, ptr);
	current = XFade2.ar(in + feedback, prev, \overdub.kr(0));
	BufWr.ar(current, bufnum, ptr);

	granPhasor = Wrap.ar(granPhasor + (\pos.kr(0) * sampsDur), startsamp, endsamp);

	polarity = ~velvet.(t, \density.kr(1), 1 - \polarityMod.kr(1));

	pan = Demand.ar(t, 0, Drand([-1, 0, 1] * \panMod.kr(1), inf));
	
	// granPhasor.poll();

	//grain output
	grains = TGrains.ar(
		numChannels: 2,
		trigger: t,
		bufnum: bufnum,
		rate: \rate.kr(1),
		centerPos: (granPhasor * BufDur.kr(bufnum)) + (dur * 0.5001),
		dur: dur,
		pan: pan,
		amp: polarity,
		interp: 4
	);

	grains = Balance2.ar(grains[0], grains[1], \pan_master.kr(0));

	//overdub feedback
	fb = grains.sum * 0.5;
	fb = LeakDC.ar(fb, 0.995);
	LocalOut.ar(fb);

	grains = grains * \gain.kr(0).dbamp;
	grains = grains * \amp.kr(1);
	Out.ar(\out.kr(0), grains);
}).add;

SynthDef(\miVerb, {
	var in, sig;
	in = InFeedback.ar(\inbus.kr(0), 2).sanitize;

	sig = MiVerb.ar(
		in,
		time: \time.kr(0.7),
		drywet: \drywet.kr(1),
		damp: \damp.kr(0.5),
		hp: \hp.kr(0.05),
		freeze: \freeze.kr(0),
		diff: \diff.kr(0.625),
	);

	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;

/*SynthDef(\reflector, {
	var in, sig;
	in = InFeedback.ar(\inbus.kr(0), 2);

	sig = Reflector.ar(
		in * 0.8,
		numReflcs: \numRefl.kr(5),
		delayOffset: \delayOffset.kr(0.1),
		scaleDelays: \scaleDelays.kr(2),
		spread: \spread.kr(0.8),
		reflPan: \reflPan.kr(0),
		lpfRefl: \lpf.kr(0.1),
		hpfRefl: \hpf.kr(250)
	);

	sig = SelectX.ar(\drywet.kr(1), [in, sig]);
	sig = sig * \gain.kr(0).dbamp;
	sig = sig * \amp.kr(1);
	sig = sig.sanitize;
	Out.ar(\out.kr(0), sig);
}).add;*/

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
	Out.ar(\out.kr(0), (\amp.kr(1) * sig * [1, 1]).sanitize);
}).add;


~compressFunc = {
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
		var numChannels=2;
		var ampMin = -70;
		var ampMax = 36;
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
			
			#compressAmt, expandAmt = ~compressFunc.(
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
				
		Out.ar(\out.kr(0), bands);
}).add;
)