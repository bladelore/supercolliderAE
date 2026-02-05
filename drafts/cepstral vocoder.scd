(
Synth(\ceptral_cross_synthesis,
	[
		recBuf: f,
		sndBuf: g
	]
);
)

(
~windowSize = 2048;
~cepbufC = Buffer.alloc(s, ~windowSize/2);
~cepbufM = Buffer.alloc(s, ~windowSize/2);
~envC = Buffer.alloc(s, ~windowSize);
~envM = Buffer.alloc(s, ~windowSize);
f = {Buffer.alloc(s, b.duration.calcPVRecSize(~fftsize, 0.5))};
g = {Buffer.alloc(s, d.duration.calcPVRecSize(~fftsize, 0.5))};
)


f
(
SynthDef(\ceptral_cross_synthesis, {
	arg out=0, pan=0, recBuf=0, sndBuf=0, posLo=0.1, posHi=0.9,
	posRateE=0, posRateM=1, amp=1, clear=0, freq=150, numharm=25, freeze=1;

	var xModEnv = \gainEnv.kr(Env.newClear(8).asArray);
	var sig, pos, posRate;
	var chain, chainM, chainC, cepschM, cepschC, inC;
	var fftSize = 2048;

	posRate = 10 ** posRateE * posRateM;

	// phasor bounds must be between 0 and 1
	pos = Phasor.ar(0, posRate * SampleDur.ir, posLo, posHi);

	// get cepstrum of modulating signal
	chainM = FFT(LocalBuf(fftSize), PlayBuf.ar(1, sndBuf, BufRateScale.kr(sndBuf)), hop:0.5, wintype:0);
	chainM = PV_BinBufRd(chainM, recBuf, pos, 0, 2, 12, clear);

	chainM = PV_Freeze(chainM, freeze);
	cepschM = Cepstrum(~cepbufM, chainM);

	chainM = chainM.pvcollect(fftSize, {
		arg mag, phase;
		var pmod;
		pmod = LFNoise1.kr(rrand(0.5, 1.1)).range(-pi, pi);
		[mag, (pmod - phase) * freeze + phase]
	},
	frombin: 0, tobin: 250, zeroothers: 1);

	// get cepstrum of carrier signal
	inC = Blip.ar(freq, numharm);
	chainC = FFT(LocalBuf(fftSize), inC, hop:0.5, wintype:0);
	cepschC = Cepstrum(~cepbufC, chainC);

	// get smooth version of modulator
	cepschM = PV_BrickWall(cepschM, \wipeM.kr(-0.92));
	ICepstrum(cepschM, ~envM);

	// get smoothed version of carrier
	cepschC = PV_BrickWall(cepschC, \wipeC.kr(0.92));
	ICepstrum(cepschC, ~envC);

	// 3. divide spectrum of each carrier frame by smooth spectral envelope (to flatten)
	chainC = PV_MagDiv(chainC, ~envC);

	// 4. multiply flattened spectral carrier frame with smooth spectral envelope of modulator
	chainC = PV_MagMul(chainC, ~envM);

	chain = PV_XFade(chainC, chainM, SinOsc.ar(0.2));

	//Bandpassfilter
	chain = PV_BrickWall(chain, \lpf.kr(20000).linlin(20,20000,-1,0));
	chain = PV_BrickWall(chain, \hpf.kr(20).linlin(20,20000,0,1));

	sig = IFFT(chain) * 35.neg.dbamp;

	sig = Splay.ar(sig, level:amp);
	sig = sig + NHHall.ar(sig, 0.3);
	sig = Limiter.ar(sig);
	Out.ar(out, sig);
}).add;
)