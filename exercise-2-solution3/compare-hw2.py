import sys
try:
    import termcolor
    def colored(col, s):
        return termcolor.colored(s, col)
except:
    def colored(col, s):
        return s

if len(sys.argv) < 3:
    print("Usage: %s tealprg.expected tealprg.out" % sys.argv[0])
    sys.exit(1)

class Line(object):
    def __init__(self, line):
        self.line = line.strip()
        self.satisfied = False

    def accept(self, l):
        if l == self.line:
            # Accept duplicates
            self.satisfied = True
            return True
        return False

    def __str__(self):
        return self.line

class OptLine(Line):
    def __init__(self, line):
        Line.__init__(self, line)
        # Don't need to see this one
        self.satisfied = True

    def __str__(self):
        return self.line + ' (optional)'

class ChoiceLine(Line):
    def __init__(self, choices):
        Line.__init__(self, '')
        self.choices = [c.strip() for c in choices]

    def accept(self, l):
        if l in self.choices:
            # Accept duplicates
            self.satisfied = True
            return True
        return False

    def __str__(self):
        return 'One of:\n- ' + '\n- '.join(self.choices)

def parse_line(string, allow_special=False):
    l = string.split()
    if len(l) == 0:
        return None

    if not allow_special and string[0] != 'N':
        return None

    if l[0] == 'maybe:':
        return OptLine(' '.join(l[1:]))
    elif l[0] == 'choice:':
        s = [p.strip() for p in (' '.join(l[1:])).split('|')]
        return ChoiceLine(s)

    if string[0] != 'N':
        print(colored('red', 'Ill-formed: %s' % string))
        return None
    return Line(string)

expected = []
with open(sys.argv[1]) as expected_file:
    for l in expected_file.readlines():
        l = l.strip()
        line = parse_line(l, True)
        if line is not None:
            expected.append(line)

def must_include(line):
    s = line.split()
    if len(s) not in [3, 4]: # report as errors later
        return True
    if s[1] == '0' and s[2] == '0':
        print('%s: skipping, since location indicates that this refers to a built-in' % line)
        return False
    return len(s) == 4 and ('+' in s[3] or '?' in s[3] or '0' in s[3])

failure = False
actual = []
with open(sys.argv[2]) as actual_file:
    pos = 0
    for line in actual_file.readlines():
        line = line.strip()
        pos += 1
        if not must_include(line):
            continue
        lp = parse_line(line, False)
        if lp is not None:
            found = False
            for e in expected:
                # first check unsatisfied
                if not e.satisfied and e.accept(line):
                    found = True
                    break
                # now check satisfied, allowing for duplicates
                if e.accept(line):
                    found = True
                    break
            if not found:
                print(colored('red', line) + ': unexpected!')
                failure = True

unsat = False
for e in expected:
    if not e.satisfied:
        if not unsat:
            unsat = True
            print("Missing expected output:")
        print(colored('yellow', str(e)))

failure = failure or unsat
if failure:
    print(colored('red', 'Specification not yet satisfied'))
    sys.exit(1)
else:
    print(colored('green', 'Clear!'))
    sys.exit(0)
