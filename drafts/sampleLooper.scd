(
SynthDef(\samplePlayer, {|buf|
	var pan = \pan.kr(0);
	var bufFrames = BufFrames.ir(buf);
	var startsamp = \start.kr(0) * bufFrames;
	var endsamp = \end.kr(1) * bufFrames;
	//channels and sampler
	var numChans = buf.numChannels;
	var sig = PlayBuf.ar(numChans, buf, BufRateScale.ir(buf) * \rate.kr(1), 0, startsamp, loop: 0);
	//handle channels
	var panned = case
	{numChans == 1} {Pan2.ar(sig, pan)}
	{numChans == 2} {Balance2.ar(sig[0], sig[1], pan)}
	{numChans > 2} { var splay = Splay.ar(sig); Balance2.ar(splay[0], splay[1], pan); } ;
	//envelope
	var dursecs = (endsamp - startsamp) / BufSampleRate.ir(buf);
	// var env = EnvGen.kr(Env.perc(\atk.kr(0.01), \rel.kr(0.5), curve: \curve.kr(-4)), gate: \gate.kr(1), doneAction:2);
	//out
	sig = panned * \gain.kr(0).dbamp;

/*	sig = SelectX.ar(\pitchMix.kr(0), [sig,
		PitchShift.ar(sig,
			pitchRatio: \pitchRatio.kr(1),
			windowSize: \windowSize.kr(0.01),
			pitchDispersion: \pitchDispersion.kr(1),
			timeDispersion: \timeDispersion.kr(0.1))]
	);*/

	Out.ar(\out.kr(0), sig);
}).add;
)

(
~sample = Buffer.read(s, "/Users/aelazary/Desktop/Samples etc./Field recs/Bell birds.wav");

)

Synth(\samplePlayer, [buf: ~sample.bufnum, \rate: 0.5, \gain: 24]);