import { Compartment, EditorState } from '@codemirror/state'
import {
  EditorView,
  crosshairCursor,
  drawSelection,
  dropCursor,
  highlightActiveLine,
  highlightActiveLineGutter,
  highlightSpecialChars,
  keymap,
  lineNumbers,
  rectangularSelection,
} from '@codemirror/view'
import {
  defaultKeymap,
  history,
  historyKeymap,
  indentLess,
  indentMore,
  insertNewlineAndIndent,
} from '@codemirror/commands'
import {
  HighlightStyle,
  StreamLanguage,
  bracketMatching,
  indentOnInput,
  indentUnit,
  syntaxHighlighting,
} from '@codemirror/language'
import {
  autocompletion,
  closeBrackets,
  closeBracketsKeymap,
  completionKeymap,
} from '@codemirror/autocomplete'
import { highlightSelectionMatches, searchKeymap } from '@codemirror/search'
import { tags as t } from '@lezer/highlight'
import { java } from '@codemirror/lang-java'
import { javascript } from '@codemirror/lang-javascript'
import { php } from '@codemirror/lang-php'
import { python } from '@codemirror/lang-python'
import { csharp } from '@codemirror/legacy-modes/mode/clike'

export const EDITOR_LANGUAGES = [
  {
    id: 'python',
    label: 'Python',
    fileName: 'solution.py',
    starter: `# one pass with a hash map
def two_sum(nums, target):
    seen = {}
    for i, n in enumerate(nums):
        if target - n in seen:
            return [seen[target - n], i]
        seen[n] = i
    return []`,
  },
  {
    id: 'javascript',
    label: 'JavaScript',
    fileName: 'solution.js',
    starter: `function twoSum(nums, target) {
  const seen = new Map();
  for (let i = 0; i < nums.length; i++) {
    const need = target - nums[i];
    if (seen.has(need)) return [seen.get(need), i];
    seen.set(nums[i], i);
  }
  return [];
}`,
  },
  {
    id: 'java',
    label: 'Java',
    fileName: 'Solution.java',
    starter: `class Solution {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int need = target - nums[i];
            if (seen.containsKey(need)) {
                return new int[] { seen.get(need), i };
            }
            seen.put(nums[i], i);
        }
        return new int[0];
    }
}`,
  },
  {
    id: 'csharp',
    label: 'C#',
    fileName: 'Solution.cs',
    starter: `public class Solution {
    public int[] TwoSum(int[] nums, int target) {
        var seen = new Dictionary<int, int>();
        for (var i = 0; i < nums.Length; i++) {
            var need = target - nums[i];
            if (seen.TryGetValue(need, out var index)) {
                return new[] { index, i };
            }
            seen[nums[i]] = i;
        }
        return Array.Empty<int>();
    }
}`,
  },
  {
    id: 'php',
    label: 'PHP',
    fileName: 'solution.php',
    starter: `<?php

function twoSum(array $nums, int $target): array {
    $seen = [];
    foreach ($nums as $i => $n) {
        $need = $target - $n;
        if (array_key_exists($need, $seen)) {
            return [$seen[$need], $i];
        }
        $seen[$n] = $i;
    }
    return [];
}`,
  },
]

const languageExtensions = {
  python: () => python(),
  javascript: () => javascript(),
  java: () => java(),
  csharp: () => StreamLanguage.define(csharp),
  php: () => php(),
}

const codeTheme = EditorView.theme(
  {
    '&': {
      height: '100%',
      color: 'var(--code-tx)',
      backgroundColor: 'var(--code-bg)',
    },
    '&.cm-focused': {
      outline: 'none',
    },
    '.cm-scroller': {
      overflow: 'auto',
      fontFamily: 'var(--f-mono)',
      fontSize: '13px',
      lineHeight: '1.65',
    },
    '.cm-content': {
      padding: 'var(--s-3) 0',
      caretColor: 'var(--tx)',
    },
    '.cm-line': {
      padding: '0 var(--s-4) 0 var(--s-2)',
    },
    '.cm-gutters': {
      color: 'var(--syn-n)',
      backgroundColor: 'var(--code-bg)',
      border: 'none',
      borderRight: '1px solid color-mix(in srgb, var(--line2) 55%, transparent)',
    },
    '.cm-lineNumbers .cm-gutterElement': {
      minWidth: '38px',
      padding: '0 10px 0 12px',
    },
    '.cm-activeLine': {
      backgroundColor: 'color-mix(in srgb, var(--tx) 4%, transparent)',
    },
    '.cm-activeLineGutter': {
      color: 'var(--tx2)',
      backgroundColor: 'transparent',
    },
    '&.cm-focused .cm-selectionBackground, .cm-selectionBackground, .cm-content ::selection': {
      backgroundColor: 'color-mix(in srgb, var(--ac) 30%, transparent) !important',
    },
    '.cm-cursor, .cm-dropCursor': {
      borderLeftColor: 'var(--tx)',
    },
    '.cm-panels': {
      color: 'var(--tx)',
      backgroundColor: 'var(--s1)',
      borderColor: 'var(--line2)',
    },
    '.cm-tooltip': {
      color: 'var(--tx)',
      backgroundColor: 'var(--s1)',
      border: '1px solid var(--line2)',
    },
    '.cm-tooltip-autocomplete > ul > li[aria-selected]': {
      color: 'var(--tx)',
      backgroundColor: 'var(--ac-soft)',
    },
  },
  { dark: true },
)

const codeHighlight = HighlightStyle.define([
  {
    tag: [t.keyword, t.modifier, t.operatorKeyword, t.definitionKeyword, t.moduleKeyword, t.self],
    color: 'var(--syn-k)',
  },
  {
    tag: [t.typeName, t.className, t.namespace, t.tagName],
    color: 'var(--syn-k)',
  },
  {
    tag: [t.function(t.variableName), t.function(t.propertyName), t.definition(t.function(t.variableName))],
    color: 'var(--ok)',
    fontWeight: '600',
  },
  {
    tag: [t.string, t.special(t.string), t.regexp, t.attributeValue],
    color: 'var(--syn-t)',
  },
  {
    tag: [t.number, t.bool, t.null, t.atom],
    color: 'var(--syn-t)',
  },
  {
    tag: [t.comment, t.lineComment, t.blockComment, t.docComment],
    color: 'var(--syn-c)',
    fontStyle: 'italic',
  },
  {
    tag: [t.variableName, t.propertyName, t.attributeName, t.labelName],
    color: 'var(--code-tx)',
  },
  {
    tag: [t.operator, t.punctuation, t.bracket],
    color: 'color-mix(in srgb, var(--code-tx) 78%, transparent)',
  },
  {
    tag: [t.meta, t.processingInstruction, t.escape],
    color: 'var(--tx2)',
  },
])

function languageExtension(id) {
  const create = languageExtensions[id] || languageExtensions.python
  return create()
}

function getLanguage(id) {
  return EDITOR_LANGUAGES.find((language) => language.id === id) || EDITOR_LANGUAGES[0]
}

function getCursor(state) {
  const position = state.selection.main.head
  const line = state.doc.lineAt(position)
  return {
    line: line.number,
    column: position - line.from + 1,
  }
}

function insertFourSpaces(view) {
  if (view.state.selection.ranges.some((range) => !range.empty)) {
    return indentMore(view)
  }

  view.dispatch({
    ...view.state.replaceSelection('    '),
    userEvent: 'input',
  })
  return true
}

function runShortcut(onRun) {
  return (view) => {
    if (typeof onRun !== 'function') return false
    onRun({
      code: view.state.doc.toString(),
      view,
    })
    return true
  }
}

export function createCodeEditor({ parent, language = 'python', doc, onRun, onUpdate }) {
  if (!parent) {
    throw new Error('A parent element is required to create the code editor.')
  }

  const languageCompartment = new Compartment()
  const run = runShortcut(onRun)
  const config = getLanguage(language)

  const state = EditorState.create({
    doc: doc ?? config.starter,
    extensions: [
      lineNumbers(),
      highlightActiveLineGutter(),
      highlightSpecialChars(),
      history(),
      drawSelection(),
      dropCursor(),
      EditorState.allowMultipleSelections.of(true),
      indentOnInput(),
      indentUnit.of('    '),
      EditorState.tabSize.of(4),
      bracketMatching(),
      closeBrackets(),
      autocompletion(),
      rectangularSelection(),
      crosshairCursor(),
      highlightActiveLine(),
      highlightSelectionMatches(),
      syntaxHighlighting(codeHighlight),
      codeTheme,
      languageCompartment.of(languageExtension(language)),
      keymap.of([
        { key: 'Mod-Enter', run },
        { key: 'Ctrl-Enter', run },
        { key: 'Enter', run: insertNewlineAndIndent },
        { key: 'Tab', run: insertFourSpaces },
        { key: 'Shift-Tab', run: indentLess },
        ...closeBracketsKeymap,
        ...defaultKeymap,
        ...searchKeymap,
        ...historyKeymap,
        ...completionKeymap,
      ]),
      EditorView.updateListener.of((update) => {
        if (!update.docChanged && !update.selectionSet) return
        onUpdate?.(getCursor(update.state))
      }),
    ],
  })

  const view = new EditorView({
    state,
    parent,
  })

  onUpdate?.(getCursor(view.state))

  return {
    view,
    getValue: () => view.state.doc.toString(),
    setValue: (value) => {
      view.dispatch({
        changes: {
          from: 0,
          to: view.state.doc.length,
          insert: value,
        },
      })
    },
    setLanguage: (id) => {
      view.dispatch({
        effects: languageCompartment.reconfigure(languageExtension(id)),
      })
    },
    focus: () => view.focus(),
    destroy: () => view.destroy(),
  }
}

export function getEditorLanguage(id) {
  return getLanguage(id)
}
