(
~pChainSwitch = {|patt, switch, mod|
    var patternList = List.new;
    if(mod.isNil.not){
        switch = switch.wrap(0, mod)
    };

    patt.do({|elem, i|
        patternList.add(
            Pbind(
                \switch, switch,
                \switchIdx, i,
                \dur, Pfunc({|ev|
                    var val;
                    if(ev[\switchIdx] == ev[\switch]){ val = ev[\dur]}{ val = Rest(ev[\dur]) };
                    val
                })
            )
            <> elem
        );
    });
    Ppar(patternList, inf);
};
    
    a = Pbind(\instrument, \kick2, \dur, 1);
    b = Pbind(\instrument, \rim1, \degree, Prand([1,3,4,5], inf), \octave, 4, \dur, 0.5);
    c = Pbind(\instrument, \hh, \degree, Prand([2,5,7,8], inf), \octave, 6, \dur, 1);
    
    ~pChainSwitch.([a, b, c], Pseq([0, 1, 2], inf)).trace.play(t);

    // Pdef(\test,
    //     ~pChainSwitch.([a, b, c], Pkey(\seq), inf))
    //     <> Pbind(\seq, Pseq([1, 0, 1, 2], inf)
    // ).trace.play(t);
)