
Synth(\padKlank_mono, [f: 30, harmonicRatio: 0.5, bw: 0.1, bwScale: 2, \stretch: 1, \detuneRate: 800, \detuneDepth: 0, \impulse: 20000, \dev: 1]);

(
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


SynthDef(\padKlank_mono, {|f=60, harmonicRatio=1, bwScale = 1, bw = 0.1|
	// var f = 60;
	var n = 8;

	var detuneRate = \detuneRate.kr(0);
	var detuneDepth = \detuneDepth.kr(0);

	var freqs = Array.newClear(n);
	var amps = Array.newClear(n);
	var ringtimes = Array.newClear(n);

	var powN, relF, hf, bw_ring;
	var in, impulse, env, modal;

	n.do{|i|
		//get harmonic integers
		powN = pow(i + 1, harmonicRatio);
		relF = (powN * (1.0 + (powN - 1)));
		//harmonic frequency
		hf = (relF * f);
		freqs[i] = hf;
		// hf.poll;
		relF = abs(relF);
		//scale ring time to frequency
		bw_ring = pow(2, (bw / 1200)) * pow(relF, bwScale);
		// bw_ring = (pow(2, (bw / 1200)) - clip(bwScale, -1 , 1)) * pow(relF, bwScale);
		ringtimes[i] = bw_ring;

	};

	impulse = GaussTrig.ar(freq: \impulse.kr(22000) ! 2, dev: \dev.kr(1)) ;
	impulse = impulse * PinkNoise.ar([1, 1]) * \inGain.kr(0).dbamp;

	// impulse = ~velvet.(Impulse.ar(\impulse.kr(5000)) ! 2, density: \dev.kr(1), bias: 0) * PinkNoise.ar([1, 1]);

	// impulse.poll;

	modal = DynKlank.ar(
		`[
			freqs + LFNoise2.ar(detuneRate! n).unipolar(detuneDepth),
			n.sqrt.reciprocal * 0.5,
			ringtimes
		],
		impulse,
		\stretch.kr(1, 0.01),
		\freqoffset.kr(0),
		\decayscale.kr(0.01)
	);

	modal = BHiShelf.ar(modal, 3000, db:-6);
	modal = BHiPass.ar(modal, 30);
	modal = BLowPass4.ar(modal, \lpf.kr(16000));
	modal = DCompressor.ar(modal, ratio: 4, threshold: -60, attack: 0.1, release: 1, makeup: 0, automakeup: 0);
	// modal = modal.softclip;
	modal = modal.sanitize;
	modal = modal * \gain.kr(-30).dbamp;
	modal = modal * \amp.kr(1);

Out.ar(\out.kr(0), modal);
}).add;
)
