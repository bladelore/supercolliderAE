(
    k = MKtl('nk2', "korg-nanokontrol2");
    ~knobState = Array.fill(8, {0});
    ~sliderState = Array.fill(8, {0});
    ~buttonState = Array.fill(5, {0});
        
    k.elAt(\kn, (0..7)).do { |knob, i|
        knob.action = {|el|
            ~knobState[i] = el.value;
            ("knob "++i++": "++el.value).postln;
        };
    };

    k.elAt(\sl, (0..7)).do { |sl, i|
        sl.action = {|el|
            ~sliderState[i] = el.value;
            ("slider "++i++": "++el.value).postln;
        }
    };


    // k.elAt(\tr, [\stop]).do{|sl| 
    //         sl.action = {|el|
    //             if (el.value == 1) { CmdPeriod.run; }
    //         }
    // };

    ~knob = {|idx| Pfunc({|ev| ~knobState.at(idx) }) };
    ~slider = {|idx| Pfunc({|ev| ~sliderState.at(idx) }) };

    ~new_advance = {
        ~advance = Condition.new;
        ~advance.test = false;
        k.elAt(\tr, [\fwd]).do{|sl| 
            sl.action = {|el|
                if (el.value == 1) {
                    ~advance.test = true;
                    ~advance.signal;
                    ~advance.test = false;
                    "next".postln;
                };
            };
        };
    };
)

// k.gui

// k = MKtl(\n, "*nanokontrol").trace;
// g = k.gui; g.parent.alwaysOnTop_(true);

// // has lots of elements, not as many as shown on gui
// k.elementsDict.size;

// // test switching gui between pages:
// // turn all knobs in group 0 to random value
// k.elAt(\kn, 0).do(_.value_(1.0.rand));
// // now switch to scene 1 - all knobs are at 0
// g.pagesSwitch.valueAction_(1);
// // turn all knobs in group 1 to random value
// k.elAt(\sl, 1).do(_.value_(1.0.rand));

// g.pagesSwitch.valueAction_(0);
// // now switch to scene 3 on faderbox,
// // move some elements ... then see if the gui shows them:
// g.pagesSwitch.valueAction_(2);

// (
// Pdef(\a,
//     Pbind(  
//         \instrument, \default,
//         \amp, ~knob.(0),
//         \dur, ~knob.(1).linlin(0, 1, 1, 32).ceil.reciprocal * 8,
//     )
// ).trace.play(t);
// )

// Pdef(\a).stop;